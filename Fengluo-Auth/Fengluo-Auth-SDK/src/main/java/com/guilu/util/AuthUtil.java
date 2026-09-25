package com.guilu.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import com.guilu.domain.Result;
import com.guilu.domain.dto.LoginUserDTO;
import com.guilu.domain.dto.PrivilegeRoleDTO;
import com.guilu.exception.RequestException.ForbiddenException;
import com.guilu.exception.RequestException.UnauthorizedException;
import com.guilu.jwt.JwtSignerHolder;
import com.guilu.utils.StringUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.guilu.constants.AuthErrorInfo.Code.EXPIRED_TOKEN_CODE;
import static com.guilu.constants.AuthErrorInfo.Code.INVALID_TOKEN_CODE;
import static com.guilu.constants.AuthErrorInfo.Msg.*;
import static com.guilu.constants.JwtConstants.AUTH_PRIVILEGE_KEY;
import static com.guilu.constants.JwtConstants.PAYLOAD_USER_KEY;

@Component
public class AuthUtil {
    // 缓存权限信息
    private Map<String, PrivilegeRoleDTO> privileges = new HashMap<>();
    // 要拦截的路径匹配符的集合
    private Set<String> paths = new HashSet<>();
    // 权限版本信息，减少不必要的缓存处理
    private int privilegeVersion;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final JwtSignerHolder jwtSignerHolder;
    private final StringRedisTemplate stringRedisTemplate;
    private final BoundHashOperations<String, String, String> hashOps;

    public AuthUtil(JwtSignerHolder jwtSignerHolder, StringRedisTemplate stringRedisTemplate) {
        this.jwtSignerHolder = jwtSignerHolder;
        this.stringRedisTemplate = stringRedisTemplate;
        this.hashOps = stringRedisTemplate.boundHashOps(AUTH_PRIVILEGE_KEY);
    }
    public Result<LoginUserDTO> parseToken(String token) {
        // 1.校验token是否为空
        if(StringUtils.isBlank(token)){
            return Result.error(INVALID_TOKEN_CODE, INVALID_TOKEN);
        }
        JWT jwt = null;
        try {
            jwt = JWT.of(token).setSigner(jwtSignerHolder.getJwtSigner());
        } catch (Exception e) {
            return Result.error(INVALID_TOKEN_CODE, INVALID_TOKEN);
        }
        // 2.校验jwt是否有效
        if (!jwt.verify()) {
            // 验证失败，返回空
            return Result.error(INVALID_TOKEN_CODE, INVALID_TOKEN);
        }
        // 3.校验是否过期
        try {
            JWTValidator.of(jwt).validateDate();
        } catch (ValidateException e) {
            return Result.error(EXPIRED_TOKEN_CODE, EXPIRED_TOKEN);
        }
        // 4.数据格式校验
        Object userPayload = jwt.getPayload(PAYLOAD_USER_KEY);
        if (userPayload == null) {
            // 数据为空
            return Result.error(INVALID_TOKEN_CODE, INVALID_TOKEN_PAYLOAD);
        }

        // 5.数据解析
        LoginUserDTO userDTO;
        try {
            userDTO = ((JSONObject)userPayload).toBean(LoginUserDTO.class);
        } catch (RuntimeException e) {
            // token格式有误
            return Result.error(INVALID_TOKEN_CODE, INVALID_TOKEN_PAYLOAD);
        }

        // 6.返回
        return Result.success(userDTO);
    }

    public void checkAuth(String antPath, Result<LoginUserDTO> r) {
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

    private String findMatchPath(String antPath){
        String matchPath = null;
        for (String pathPattern : paths) {
            if(antPathMatcher.match(pathPattern, antPath)){
                matchPath = pathPattern;
                break;
            }
        }
        return matchPath;
    }

    private PrivilegeRoleDTO findPathPrivilege(String path){
        return privileges.get(path);
    }
}
