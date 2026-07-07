package com.huawei.devbridge.relaycontroller.infrastructure.redis;

import com.huawei.devbridge.relaycontroller.domain.model.JwtToken;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenCache {
    private static final String TOKEN_KEY_PREFIX = "jwt:token:";

    private final StringRedisTemplate stringRedisTemplate;

    @Nullable
    public JwtToken getToken(String tunnelId) {
        try {
            String key = TOKEN_KEY_PREFIX + tunnelId;
            String token = stringRedisTemplate.opsForValue().get(key);
            if (token == null || token.isBlank()) {
                return null;
            }
            Long ttlSeconds = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttlSeconds == null || ttlSeconds <= 0) {
                return null;
            }
            return new JwtToken(token, ttlSeconds);
        } catch (RuntimeException ignored) {
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
