package com.guilu.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.guilu.domain.dto.LoginRequest;
import com.guilu.domain.dto.LoginUserDTO;
import com.guilu.domain.dto.TokenPair;
import com.guilu.domain.po.SysUser;
import com.guilu.exception.RequestException.UnauthorizedException;
import com.guilu.mapper.SysUserMapper;
import com.guilu.mapper.SysUserRoleMapper;
import com.guilu.service.AccountService;
import com.guilu.service.TokenService;
import com.guilu.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.guilu.constants.AuthErrorInfo.Msg.INVALID_TOKEN;

/**
 * 账号认证实现。
 * <p>
 * 逻辑删除：Nacos 的 sharding-mysql.yaml 配置了
 * {@code mybatis-plus.global-config.db-config.logic-delete-field: deleted}，
 * MyBatis-Plus 会自动为 BaseMapper/Wrapper 查询追加 deleted = 0。
 * 这里仍显式写出该条件，避免登录这条安全敏感路径依赖外部配置是否加载成功。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    /** 登录失败统一提示，不区分用户名不存在与密码错误，避免泄露账号是否存在 */
    private static final String LOGIN_FAILED = "用户名或密码错误";
    private static final String ACCOUNT_DISABLED = "账号已被禁用";

    /** sys_user.last_login_ip 为 varchar(64) */
    private static final int IP_MAX_LENGTH = 64;

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final TokenService tokenService;

    @Override
    public TokenPair login(LoginRequest request, HttpServletRequest httpRequest) {
        // 1.参数校验
        if (request == null
                || StringUtils.isBlank(request.getUsername())
                || StringUtils.isBlank(request.getPassword())) {
            throw new UnauthorizedException(LOGIN_FAILED);
        }
        // 2.查询用户
        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername().trim())
                .eq(SysUser::getDeleted, 0)
                .last("LIMIT 1"));
        // 3.校验密码（password_hash 可能为空，例如仅第三方登录的账号）
        if (user == null || !matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException(LOGIN_FAILED);
        }
        // 4.校验状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new UnauthorizedException(ACCOUNT_DISABLED);
        }
        // 5.解析写入 token 的角色（一人一角色，取 sort 最小的启用角色）
        Long roleId = sysUserRoleMapper.selectPrimaryRoleId(user.getId());
        if (roleId == null) {
            log.warn("用户 {} 未绑定任何启用角色，登录后所有受权限控制的路径都会被拒绝", user.getId());
        }
        // 6.签发双 token 并记录登录信息
        LoginUserDTO loginUser = new LoginUserDTO();
        loginUser.setUserId(user.getId());
        loginUser.setRoleId(roleId);
        loginUser.setRememberMe(request.getRememberMe());
        TokenPair tokenPair = tokenService.issue(loginUser);
        recordLogin(user.getId(), httpRequest);
        log.info("用户 {} 登录成功, roleId={}, rememberMe={}",
                user.getId(), roleId, request.getRememberMe());
        return tokenPair;
    }

    @Override
    public TokenPair refresh(String refreshToken) {
        if (StringUtils.isBlank(refreshToken)) {
            throw new UnauthorizedException(INVALID_TOKEN);
        }
        return tokenService.refresh(refreshToken);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        if (StringUtils.isBlank(accessToken) && StringUtils.isBlank(refreshToken)) {
            // 两个 token 都未携带，无需吊销
            return;
        }
        tokenService.revoke(accessToken, refreshToken);
    }

    /**
     * BCrypt 比对。哈希串格式非法时按密码错误处理，不向上抛异常。
     */
    private boolean matches(String rawPassword, String passwordHash) {
        if (StringUtils.isBlank(passwordHash)) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, passwordHash);
        } catch (IllegalArgumentException e) {
            log.warn("password_hash 不是合法的 BCrypt 摘要");
            return false;
        }
    }

    /**
     * 记录最近一次登录时间与IP，失败不影响登录结果。
     * 显式指定要更新的两列，不依赖全局 field-strategy 的取值：
     * Nacos 里的 {@code global-config.field-strategy} 在 MyBatis-Plus 3.5.x 已不存在
     * （被 insert/update/where-strategy 取代），实际生效的是默认的 NOT_NULL。
     */
    private void recordLogin(Long userId, HttpServletRequest httpRequest) {
        try {
            sysUserMapper.update(null, Wrappers.<SysUser>lambdaUpdate()
                    .eq(SysUser::getId, userId)
                    .set(SysUser::getLastLoginAt, LocalDateTime.now())
                    .set(SysUser::getLastLoginIp, resolveClientIp(httpRequest)));
        } catch (Exception e) {
            log.error("记录用户 {} 登录信息失败", userId, e);
        }
    }

    /**
     * 解析客户端IP：网关会追加 X-Forwarded-For，末段由本系统网关写入、客户端无法伪造，
     * 因此取末段；无该头时回退到 TCP 对端地址。结果按列宽截断。
     */
    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader(X_FORWARDED_FOR);
        if (StringUtils.isNotBlank(forwarded)) {
            String[] parts = forwarded.split(",");
            String last = parts[parts.length - 1].trim();
            if (StringUtils.isNotBlank(last)) {
                return truncate(last);
            }
        }
        return truncate(request.getRemoteAddr());
    }

    private String truncate(String value) {
        if (value == null || value.length() <= IP_MAX_LENGTH) {
            return value;
        }
        return value.substring(0, IP_MAX_LENGTH);
    }
}
