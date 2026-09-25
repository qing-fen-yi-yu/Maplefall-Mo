package com.guilu.service;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import com.guilu.constants.JwtConstants;
import com.guilu.domain.Result;
import com.guilu.domain.dto.LoginUserDTO;
import com.guilu.domain.dto.TokenPair;
import com.guilu.exception.RequestException.UnauthorizedException;
import com.guilu.jwt.JwtSignerHolder;
import com.guilu.store.TokenStore;
import com.guilu.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static com.guilu.constants.AuthErrorInfo.Code.EXPIRED_TOKEN_CODE;
import static com.guilu.constants.AuthErrorInfo.Code.INVALID_TOKEN_CODE;
import static com.guilu.constants.AuthErrorInfo.Msg.EXPIRED_TOKEN;
import static com.guilu.constants.AuthErrorInfo.Msg.INVALID_TOKEN;
import static com.guilu.constants.AuthErrorInfo.Msg.INVALID_TOKEN_PAYLOAD;
import static com.guilu.constants.JwtConstants.PAYLOAD_JTI_KEY;
import static com.guilu.constants.JwtConstants.PAYLOAD_USER_KEY;

/**
 * token 的签发、校验、刷新与注销。
 */
@Component
@RequiredArgsConstructor
public class TokenService {

    private final JwtSignerHolder jwtSignerHolder;
    private final TokenStore tokenStore;

    /**
     * 校验签名与有效期，并解析用户信息（不含 Redis 有效性校验）。
     */
    public Result<LoginUserDTO> verify(String token) {
        if (StringUtils.isBlank(token)) {
            return Result.error(INVALID_TOKEN_CODE, INVALID_TOKEN);
        }
        JWT jwt;
        try {
            jwt = JWT.of(token).setSigner(jwtSignerHolder.getJwtSigner());
        } catch (Exception e) {
            return Result.error(INVALID_TOKEN_CODE, INVALID_TOKEN);
        }
        // 校验jwt是否有效
        if (!jwt.verify()) {
            return Result.error(INVALID_TOKEN_CODE, INVALID_TOKEN);
        }
        // 校验是否过期
        try {
            JWTValidator.of(jwt).validateDate();
        } catch (ValidateException e) {
            return Result.error(EXPIRED_TOKEN_CODE, EXPIRED_TOKEN);
        }
        // 数据格式校验
        Object userPayload = jwt.getPayload(PAYLOAD_USER_KEY);
        if (userPayload == null) {
            return Result.error(INVALID_TOKEN_CODE, INVALID_TOKEN_PAYLOAD);
        }
        LoginUserDTO user;
        try {
            user = ((JSONObject) userPayload).toBean(LoginUserDTO.class);
        } catch (RuntimeException e) {
            return Result.error(INVALID_TOKEN_CODE, INVALID_TOKEN_PAYLOAD);
        }
        return Result.success(user);
    }

    /**
     * 取出 token 中的 jti，解析失败返回 null。
     */
    public String jti(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        try {
            Object jti = JWT.of(token).getPayload(PAYLOAD_JTI_KEY);
            return jti == null ? null : jti.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 签发 access + refresh token，并把 token 状态记录到 Redis。
     */
    public TokenPair issue(LoginUserDTO user) {
        Duration accessTtl = JwtConstants.JWT_TOKEN_TTL;
        Duration refreshTtl = Boolean.TRUE.equals(user.getRememberMe())
                ? JwtConstants.JWT_REMEMBER_ME_TTL
                : JwtConstants.JWT_REFRESH_TTL;
        String accessJti = newJti();
        String refreshJti = newJti();
        String accessToken = createToken(user, accessJti, accessTtl);
        String refreshToken = createToken(user, refreshJti, refreshTtl);
        tokenStore.saveAccess(user.getUserId(), accessJti, accessTtl);
        tokenStore.saveRefresh(user.getUserId(), refreshJti, refreshTtl);
        return new TokenPair(accessToken, refreshToken, accessTtl.getSeconds(), refreshTtl.getSeconds());
    }

    /**
     * 用 refresh token 换取新的 token 对（刷新令牌旋转，旧 refresh token 立即失效）。
     */
    public TokenPair refresh(String refreshToken) {
        Result<LoginUserDTO> result = verify(refreshToken);
        if (!result.isLoginStatus()) {
            throw new UnauthorizedException(result.getMessage(), result.getCode());
        }
        LoginUserDTO user = result.getData();
        String jti = jti(refreshToken);
        if (!tokenStore.isRefreshActive(user.getUserId(), jti)) {
            throw new UnauthorizedException(INVALID_TOKEN);
        }
        tokenStore.revokeRefresh(user.getUserId(), jti);
        return issue(user);
    }

    /**
     * 注销：同时吊销 access 与 refresh token。
     */
    public void revoke(String accessToken, String refreshToken) {
        revokeAccess(accessToken);
        revokeRefresh(refreshToken);
    }

    private void revokeAccess(String token) {
        Result<LoginUserDTO> result = verify(token);
        if (result.isLoginStatus()) {
            tokenStore.revokeAccess(result.getData().getUserId(), jti(token));
        }
    }

    private void revokeRefresh(String token) {
        Result<LoginUserDTO> result = verify(token);
        if (result.isLoginStatus()) {
            tokenStore.revokeRefresh(result.getData().getUserId(), jti(token));
        }
    }

    private String createToken(LoginUserDTO user, String jti, Duration ttl) {
        Instant now = Instant.now();
        return JWT.create()
                .setPayload(PAYLOAD_USER_KEY, user)
                .setPayload(PAYLOAD_JTI_KEY, jti)
                .setIssuedAt(Date.from(now))
                .setExpiresAt(Date.from(now.plus(ttl)))
                .setSigner(jwtSignerHolder.getSignSigner())
                .sign();
    }

    private String newJti() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
