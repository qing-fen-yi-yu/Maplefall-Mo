package com.guilu.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.guilu.constants.JwtConstants;
import com.guilu.constants.ResultInfo;
import com.guilu.domain.Enum.CodeTypeEnum;
import com.guilu.domain.Result;
import com.guilu.domain.dto.LoginRequest;
import com.guilu.domain.dto.LoginUserDTO;
import com.guilu.domain.dto.RegisterRequest;
import com.guilu.domain.dto.TokenPair;
import com.guilu.domain.dto.update.PasswordUpdateRequest;
import com.guilu.domain.dto.update.UserInfo;
import com.guilu.domain.dto.update.UserUpdateRequest;
import com.guilu.domain.po.SysUser;
import com.guilu.domain.vo.ImageCodeVO;
import com.guilu.exception.RequestException.BadRequestException;
import com.guilu.exception.RequestException.UnauthorizedException;
import com.guilu.service.*;
import com.guilu.util.PasswordEncoder;
import com.guilu.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.guilu.constants.AuthErrorInfo.Msg.INVALID_TOKEN;
import static com.guilu.constants.ErrConstants.ACCOUNT.*;
import static com.guilu.constants.ErrConstants.USER.NEW_PASSWORD_REQUIRED;
import static com.guilu.constants.ErrConstants.USER.OLD_PASSWORD_REQUIRED;
import static com.guilu.constants.ErrConstants.USER.OLD_PASSWORD_WRONG;
import static com.guilu.constants.ErrConstants.USER.PASSWORD_UNCHANGED;

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

    /**
     * 用户不存在时用来消耗等量 BCrypt 时间的占位摘要。
     */
    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$scP/ehLaVykNmHgc3PGRE./T4BfygSviT8z2myTTq9e2Bq4eljuXK";

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
        if (vailUserCode(request.getCode(),request.getCodeId())){
            throw new UnauthorizedException("验证码错误");
        }
        // 2.查询用户
        SysUser user = userService.getBaseMapper().selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername().trim())
                .eq(SysUser::getDeleted, 0)
                .last("LIMIT 1"));
        // 3.校验密码
        String storedHash = user == null ? null : user.getPassword();
        String passwordHash = StringUtils.isBlank(storedHash) ? DUMMY_PASSWORD_HASH : storedHash;
        boolean passwordOk = matches(request.getPassword(), passwordHash);
        if (user == null || !passwordOk) {
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
    @Transactional(rollbackFor = Exception.class)
    public TokenPair registerUser(RegisterRequest regisUser, HttpServletRequest request) {
        if (!imageCodeService.verifyCode(regisUser.getCode(),regisUser.getCodeId())) {
            throw new UnauthorizedException(ResultInfo.Msg.INVALID_VERIFY_CODE);
        }
        String username = regisUser.getUsername().trim();
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException(ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        }
        String email = StringUtils.isBlank(regisUser.getEmail()) ? null : regisUser.getEmail().trim();
        assertNotRegistered(username, email);

        SysUser sysUser = BeanUtils.copyBean(regisUser, SysUser.class);
        sysUser.setUsername(username);
        sysUser.setNickname(username);
        sysUser.setEmail(email);
        sysUser.setCreatedAt(LocalDateTime.now());
        sysUser.setUpdatedAt(LocalDateTime.now());
        sysUser.setLastLoginAt(LocalDateTime.now());
        sysUser.setLastLoginIp(resolveClientIp(request));
        sysUser.setPassword(PasswordEncoder.encode(sysUser.getPassword()));
        try {
            userService.save(sysUser);
            userRoleService.bindDefaultRole(sysUser.getId());
        } catch (DuplicateKeyException e) {
            throw new BadRequestException(REGISTER_CONFLICT);
        }
        Long roleId = userRoleService.selectPrimaryRoleId(sysUser.getId());
        LoginUserDTO userDTO = new LoginUserDTO();
        userDTO.setUserId(sysUser.getId());
        userDTO.setRememberMe(false);
        userDTO.setRoleId(roleId);
        return tokenService.issue(userDTO);
    }

    @Override
    public ImageCodeVO createCode(CodeTypeEnum typeEnum) {
        if(ObjectUtils.hasNull(typeEnum)){
            typeEnum = CodeTypeEnum.LINECAPTCHA;
        }
        return imageCodeService.createImageCode(typeEnum);
    }

    @Override
    public void updateMyInfo(UserInfo userInfo, HttpServletRequest request) {
        AssertUtils.isNotNull(userInfo, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        UserUpdateRequest update = new UserUpdateRequest();
        update.setId(resolveCurrentUserId(request));
        update.setNickname(userInfo.getNickname());
        update.setEmail(userInfo.getEmail());
        update.setPhone(userInfo.getPhone());
        update.setAvatarUrl(userInfo.getAvatarUrl());
        userService.updateUser(update);
    }

    @Override
    public void changePassword(PasswordUpdateRequest request, HttpServletRequest httpRequest) {
        AssertUtils.isNotNull(request, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        AssertUtils.isNotBlank(request.getOldPassword(), OLD_PASSWORD_REQUIRED);
        AssertUtils.isNotBlank(request.getNewPassword(), NEW_PASSWORD_REQUIRED);
        Long userId = resolveCurrentUserId(httpRequest);
        SysUser user = userService.getById(userId);
        AssertUtils.isNotNull(user, ResultInfo.Msg.USER_NOT_EXISTS);
        if (!matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException(OLD_PASSWORD_WRONG);
        }
        if (matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException(PASSWORD_UNCHANGED);
        }
        // 加密交给用户服务完成，这里只传递明文，避免调用方误把明文写库
        userService.resetPassword(userId, request.getNewPassword());
    }

    /**
     * 解析登录用户ID
     */
    private Long resolveCurrentUserId(HttpServletRequest request) {
        Long userId = UserContext.getUser();
        if (userId != null) {
            return userId;
        }
        String access = request == null ? null : request.getHeader(JwtConstants.AUTHORIZATION_HEADER);
        String refresh = request == null ? null : request.getHeader(JwtConstants.REFRESH_HEADER);
        userId = resolveUserId(access, refresh);
        if (userId == null) {
            throw new UnauthorizedException(INVALID_TOKEN);
        }
        return userId;
    }

    /**
     * 唯一性校验收敛到用户服务，注册与后台「新增用户」共用同一套规则。
     */
    private void assertNotRegistered(String username, String email) {
        userService.assertNotRegistered(username, email, null);
    }

    /**
     * 从 access/refresh token 解析当前用户，仅用于日志
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
    private Boolean vailUserCode(String code,String id){
        return BooleanUtils.isTrue(imageCodeService.verifyCode(code,id));
    }
}
