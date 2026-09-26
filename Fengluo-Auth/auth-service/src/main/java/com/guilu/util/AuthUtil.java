package com.guilu.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.guilu.domain.Result;
import com.guilu.domain.dto.LoginUserDTO;
import com.guilu.domain.dto.PrivilegeRoleDTO;
import com.guilu.exception.RequestException.ForbiddenException;
import com.guilu.exception.RequestException.UnauthorizedException;
import com.guilu.service.TokenService;
import com.guilu.store.TokenStore;
import com.guilu.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.guilu.constants.AuthErrorInfo.Code.INVALID_TOKEN_CODE;
import static com.guilu.constants.AuthErrorInfo.Msg.FORBIDDEN;
import static com.guilu.constants.AuthErrorInfo.Msg.INVALID_TOKEN;
import static com.guilu.constants.JwtConstants.AUTH_PRIVILEGE_KEY;
import static com.guilu.constants.JwtConstants.AUTH_PRIVILEGE_VERSION_KEY;

@Slf4j
@Component
public class AuthUtil {
    private static final long PRIVILEGE_REFRESH_INTERVAL_MILLIS = 30_000L;

    // 缓存权限信息
    private volatile Map<String, PrivilegeRoleDTO> privileges = new HashMap<>();
    // 要拦截的路径匹配符的集合
    private volatile Set<String> paths = new HashSet<>();
    // 权限版本信息，减少不必要的缓存处理
    private volatile int privilegeVersion = -1;
    // 上次检查权限版本的时刻
    private volatile long lastPrivilegeRefreshAt = 0L;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final StringRedisTemplate stringRedisTemplate;
    private final BoundHashOperations<String, String, String> hashOps;
    private final TokenService tokenService;
    private final TokenStore tokenStore;

    public AuthUtil(StringRedisTemplate stringRedisTemplate,
                    TokenService tokenService,
                    TokenStore tokenStore) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.hashOps = stringRedisTemplate.boundHashOps(AUTH_PRIVILEGE_KEY);
        this.tokenService = tokenService;
        this.tokenStore = tokenStore;
    }

    public Result<LoginUserDTO> parseToken(String token) {
        // 1.校验签名与有效期并解析用户信息
        Result<LoginUserDTO> result = tokenService.verify(token);
        if (!result.isLoginStatus()) {
            return result;
        }
        // 2.网关侧统一校验 access token 在 Redis 中仍然有效（支持注销/吊销），
        //   集中在此处访问 Redis，避免各业务模块重复访问
        String jti = tokenService.jti(token);
        if (!tokenStore.isAccessActive(result.getData().getUserId(), jti)) {
            return Result.error(INVALID_TOKEN_CODE, INVALID_TOKEN);
        }
        return result;
    }

    public void checkAuth(String antPath, Result<LoginUserDTO> r) {
        // 0.确保本地权限缓存是最新的
        refreshPrivileges();
        // 1.判断是否是需要权限的路径
        String matchPath = findMatchPath(antPath);
        if(matchPath == null){
            // 没有权限限制，直接放行
            return;
        }
        // 2.判断是否登录成功
        if(!r.isLoginStatus()){
            // 未登录，直接报错
            throw new UnauthorizedException(r.getMessage(), r.getCode());
        }
        // 3.获取当前路径所需权限
        PrivilegeRoleDTO pathPrivilege = findPathPrivilege(matchPath);

        // 4.权限判断
        Set<Long> requiredRoles = pathPrivilege.getRoles();
        if (!CollectionUtil.contains(requiredRoles, r.getData().getRoleId())) {
            // 没有访问权限
            throw new ForbiddenException(FORBIDDEN);
        }
    }

    /**
     * 从 Redis（auth:privileges）刷新本地权限缓存，版本号未变化时直接返回。
     * Redis 不可用时保留上一次的缓存，不阻断请求（token 校验仍会拦截未登录请求）。
     */
    public void refreshPrivileges() {
        long now = System.currentTimeMillis();
        if (now - lastPrivilegeRefreshAt < PRIVILEGE_REFRESH_INTERVAL_MILLIS) {
            return;
        }
        lastPrivilegeRefreshAt = now;
        try {
            int version = parseVersion(hashOps.get(AUTH_PRIVILEGE_VERSION_KEY));
            if (version == privilegeVersion) {
                return;
            }
            Map<String, String> entries = hashOps.entries();
            Map<String, PrivilegeRoleDTO> newPrivileges = new HashMap<>();
            Set<String> newPaths = new HashSet<>();
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                if (AUTH_PRIVILEGE_VERSION_KEY.equals(entry.getKey())) {
                    continue;
                }
                PrivilegeRoleDTO dto;
                try {
                    dto = JSONUtil.toBean(entry.getValue(), PrivilegeRoleDTO.class);
                } catch (RuntimeException e) {
                    log.warn("解析权限缓存失败, field={}", entry.getKey(), e);
                    continue;
                }
                if (dto == null || StringUtils.isBlank(dto.getAntPath())) {
                    continue;
                }
                newPrivileges.put(dto.getAntPath(), dto);
                newPaths.add(dto.getAntPath());
            }
            this.privileges = newPrivileges;
            this.paths = newPaths;
            this.privilegeVersion = version;
            log.debug("权限缓存刷新完成, version={}, 共 {} 条", version, newPaths.size());
        } catch (Exception e) {
            log.error("刷新权限缓存失败", e);
        }
    }

    private int parseVersion(String value) {
        if (StringUtils.isBlank(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 找出匹配该请求的权限路径。
     * paths 来自 Redis HASH，遍历顺序不确定；原先「取第一个命中」会让同时命中多条规则时
     * 生效的规则随机，可能放宽也可能收紧权限。改为「取最长的匹配模式」，
     * 即最具体的规则优先，结果确定，也符合具体优于笼统的惯例。
     */
    private String findMatchPath(String antPath) {
        String matchPath = null;
        for (String pathPattern : paths) {
            if (antPathMatcher.match(pathPattern, antPath)
                    && (matchPath == null || pathPattern.length() > matchPath.length())) {
                matchPath = pathPattern;
            }
        }
        return matchPath;
    }

    private PrivilegeRoleDTO findPathPrivilege(String path){
        return privileges.get(path);
    }
}
