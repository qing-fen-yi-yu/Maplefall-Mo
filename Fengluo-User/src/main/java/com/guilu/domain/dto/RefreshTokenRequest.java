package com.guilu.domain.dto;

import lombok.Data;

/**
 * 刷新 token 请求参数。
 */
@Data
public class RefreshTokenRequest {

    /** 登录/上次刷新时下发的 refresh token */
    private String refreshToken;
}
