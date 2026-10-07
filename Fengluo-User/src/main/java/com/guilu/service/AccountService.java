package com.guilu.service;

import com.guilu.domain.Enum.CodeTypeEnum;
import com.guilu.domain.dto.LoginRequest;
import com.guilu.domain.dto.RegisterRequest;
import com.guilu.domain.dto.TokenPair;
import com.guilu.domain.dto.update.PasswordUpdateRequest;
import com.guilu.domain.dto.update.UserInfo;
import com.guilu.domain.vo.ImageCodeVO;
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

    ImageCodeVO createCode(CodeTypeEnum codeTypeEnum);

    void updateMyInfo(UserInfo userInfo, HttpServletRequest request);

    /**
     * 修改当前登录用户的密码：校验原密码后写入新密码摘要。
     * <p>
     * 注意：已签发的 access / refresh token <b>不会</b>因此失效，它们仍在使用期内可用。
     * 如需「改密即下线」需要联动 TokenStore，属于后续独立改动。
     * </p>
     *
     * @param request     原密码 + 新密码
     * @param httpRequest 用于在 UserContext 缺失时从 token 头解析当前用户
     */
    void changePassword(PasswordUpdateRequest request, HttpServletRequest httpRequest);
}
