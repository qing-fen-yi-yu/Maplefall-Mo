package com.guilu.util;

import com.guilu.utils.StringUtils;
import org.springframework.security.crypto.bcrypt.BCrypt;

public final class PasswordEncoder {
    /** 成本因子：10 ≈ 单次 50-100ms。调高线性拖慢每次登录，别放进循环 */
    private static final int STRENGTH = 10;

    private PasswordEncoder() {}

    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(STRENGTH));
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (StringUtils.isBlank(rawPassword) || StringUtils.isBlank(encodedPassword)) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }
}

