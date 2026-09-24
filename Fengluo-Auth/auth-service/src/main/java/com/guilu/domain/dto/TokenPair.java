package com.guilu.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 双 token：access token 用于访问，refresh token 用于换取新的 token 对。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenPair {
    private String accessToken;
    private String refreshToken;
    /** access token 有效期（秒） */
    private long accessExpiresIn;
    /** refresh token 有效期（秒） */
    private long refreshExpiresIn;
}
