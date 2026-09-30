package com.guilu.service;

import com.guilu.domain.dto.LoginRequest;
import com.guilu.domain.dto.RegisterRequest;
import com.guilu.domain.dto.TokenPair;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 账号认证：登录签发双 token、刷新、注销。
 * <p>
 * 校验与签发逻辑全部复用 auth-service 的 {@code TokenService}，
 * 本接口只负责用户数据（密码比对、角色解析）与登录信息记录。
 */
public interface AccountService {

    /**
     * 用户名密码登录，签发 access + refresh token。
     *
     * @param request     登录参数
     * @param httpRequest 用于记录登录IP，可为 null
     * @return 新签发的 token 对
     */
    TokenPair login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * 用 refresh token 换取新的 token 对（旧 refresh token 立即失效）。
     *
     * @param refreshToken 上次下发的 refresh token
     * @return 新签发的 token 对
     */
    TokenPair refresh(String refreshToken);

    /**
     * 注销：吊销 access 与 refresh token，使二者无法再通过校验。
     *
     * @param accessToken  当前 access token，可为 null
     * @param refreshToken 当前 refresh token，可为 null
     */
    void logout(String accessToken, String refreshToken);

    TokenPair registerUser(RegisterRequest regisUser, HttpServletRequest request);
}
