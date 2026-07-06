package com.huawei.devbridge.relaycontroller.infrastructure.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenCache {
    private static final String TOKEN_KEY_PREFIX = "jwt:token:";

    private final StringRedisTemplate stringRedisTemplate;

    public String getToken(String tunnelId) {
        try {
            return stringRedisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + tunnelId);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public void setToken(String tunnelId, String token, long ttlSeconds) {
        try {
            stringRedisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + tunnelId, token, Duration.ofSeconds(ttlSeconds));
        } catch (RuntimeException ignored) {
            // Redis is a cache here; token generation remains functional if Redis is unavailable.
        }
    }

    public void deleteToken(String tunnelId) {
        try {
            stringRedisTemplate.delete(TOKEN_KEY_PREFIX + tunnelId);
        } catch (RuntimeException ignored) {
        }
    }
}
