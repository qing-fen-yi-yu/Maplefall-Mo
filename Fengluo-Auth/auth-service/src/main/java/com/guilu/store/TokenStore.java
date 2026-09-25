package com.guilu.store;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static com.guilu.constants.JwtConstants.JWT_REDIS_KEY_PREFIX;

/**
 * 基于 Redis 的 token 状态存储（双 token 机制）。
 * 签发时写入，网关侧校验 access token 是否仍有效，注销/刷新时删除。
 * key 形如 jwt:uid:{userId}:access:{jti}
 */
@Component
@RequiredArgsConstructor
public class TokenStore {

    private static final String ACCESS = ":access:";
    private static final String REFRESH = ":refresh:";

    private final StringRedisTemplate stringRedisTemplate;

    public void saveAccess(Long userId, String jti, Duration ttl) {
        stringRedisTemplate.opsForValue().set(accessKey(userId, jti), String.valueOf(userId), ttl);
    }

    public void saveRefresh(Long userId, String jti, Duration ttl) {
        stringRedisTemplate.opsForValue().set(refreshKey(userId, jti), String.valueOf(userId), ttl);
    }

    public boolean isAccessActive(Long userId, String jti) {
        return jti != null && Boolean.TRUE.equals(stringRedisTemplate.hasKey(accessKey(userId, jti)));
    }

    public boolean isRefreshActive(Long userId, String jti) {
        return jti != null && Boolean.TRUE.equals(stringRedisTemplate.hasKey(refreshKey(userId, jti)));
    }

    public void revokeAccess(Long userId, String jti) {
        if (jti != null) {
            stringRedisTemplate.delete(accessKey(userId, jti));
        }
    }

    public void revokeRefresh(Long userId, String jti) {
        if (jti != null) {
            stringRedisTemplate.delete(refreshKey(userId, jti));
        }
    }

    private String accessKey(Long userId, String jti) {
        return JWT_REDIS_KEY_PREFIX + userId + ACCESS + jti;
    }

    private String refreshKey(Long userId, String jti) {
        return JWT_REDIS_KEY_PREFIX + userId + REFRESH + jti;
    }
}
