package com.guilu.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.guilu.constants.ResultInfo;
import com.guilu.domain.Result;
import com.guilu.domain.dto.LoginRequest;
import com.guilu.domain.dto.LoginUserDTO;
import com.guilu.domain.dto.RegisterRequest;
import com.guilu.domain.dto.TokenPair;
import com.guilu.domain.po.SysUser;
import com.guilu.exception.RequestException.UnauthorizedException;
import com.guilu.service.*;
import com.guilu.util.PasswordEncoder;
import com.guilu.utils.BeanUtils;
import com.guilu.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.guilu.constants.AuthErrorInfo.Msg.INVALID_TOKEN;

/**
 * 账号认证实现。
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

    private final ISysUserService userService;
    private final ISysUserRoleService userRoleService;
    private final TokenService tokenService;
    private final ImageCodeService imageCodeService;

    @Override
    public TokenPair login(LoginRequest request, HttpServletRequest httpRequest) {
        // 1.参数校验
        if (request == null
                || StringUtils.isBlank(request.getUsername())
                || StringUtils.isBlank(request.getPassword())) {
            throw new UnauthorizedException(LOGIN_FAILED);
        }
        // 2.查询用户
        SysUser user = userService.getBaseMapper().selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername().trim())
                .eq(SysUser::getDeleted, 0)
                .last("LIMIT 1"));
        // 3.校验密码（password_hash 可能为空，例如仅第三方登录的账号）
        if (user == null || !matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(LOGIN_FAILED);
        }
        // 4.校验状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new UnauthorizedException(ACCOUNT_DISABLED);
        }
        // 5.解析写入 token 的角色
        Long roleId = userRoleService.selectPrimaryRoleId(user.getId());
        if (roleId == null) {
            log.warn("用户 {} 未绑定任何启用角色", user.getId());
        }
        // 6.签发双 token 并记录登录信息
        LoginUserDTO loginUser = new LoginUserDTO();
        loginUser.setUserId(user.getId());
        loginUser.setRoleId(roleId);
        loginUser.setRememberMe(request.getRememberMe());
        TokenPair tokenPair = tokenService.issue(loginUser);
        recordLogin(user.getId(), httpRequest);
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
        resolveUserId(accessToken, refreshToken);
        tokenService.revoke(accessToken, refreshToken);
    }

    @Override
    public TokenPair registerUser(RegisterRequest regisUser, HttpServletRequest request) {
        if(imageCodeService.verifyCode(regisUser.getCode())){
            throw new UnauthorizedException(ResultInfo.Msg.INVALID_VERIFY_CODE);
        }
        SysUser sysUser = BeanUtils.copyBean(regisUser, SysUser.class);
        sysUser.setCreatedAt(LocalDateTime.now());
        sysUser.setUpdatedAt(LocalDateTime.now());
        sysUser.setLastLoginAt(LocalDateTime.now());
        sysUser.setLastLoginIp(resolveClientIp(request));
        sysUser.setPassword(PasswordEncoder.encode(sysUser.getPassword()));
        userService.save(sysUser);
        LoginUserDTO userDTO = new LoginUserDTO();
        userDTO.setUserId(sysUser.getId());
        userDTO.setRoleId(0L);
        userDTO.setRememberMe(false);
        return tokenService.issue(userDTO);
    }

    /**
     * 从 access/refresh token 尽力解析当前用户，仅用于日志，失败返回 null。
     * 注销接口在网关的排除登录列表内（token 过期也应能注销），网关注入的 user-info 头
     * 对注销请求不存在，因此这里不能依赖 UserContext，只能从 token 自身解析身份。
     */
    private Long resolveUserId(String accessToken, String refreshToken) {
        Result<LoginUserDTO> r = tokenService.verify(accessToken);
        if (r.isLoginStatus()) {
            return r.getData().getUserId();
        }
        r = tokenService.verify(refreshToken);
        return r.isLoginStatus() ? r.getData().getUserId() : null;
    }

    /**
     * 密码校验
     * @param rawPassword --用户的密码
     * @param passwordHash --加密密码
     */
    private boolean matches(String rawPassword, String passwordHash) {
        if (StringUtils.isBlank(passwordHash)) {
            return false;
        }
        try {
            return PasswordEncoder.matches(rawPassword, passwordHash);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 记录最近一次登录时间与IP，失败不影响登录结果。
     */
    private void recordLogin(Long userId, HttpServletRequest httpRequest) {
        try {
            userService.getBaseMapper().update(null, Wrappers.<SysUser>lambdaUpdate()
                    .eq(SysUser::getId, userId)
                    .set(SysUser::getLastLoginAt, LocalDateTime.now())
                    .set(SysUser::getLastLoginIp, resolveClientIp(httpRequest)));
        } catch (Exception e) {
            log.warn("记录用户 {} 登录信息失败", userId, e);
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
