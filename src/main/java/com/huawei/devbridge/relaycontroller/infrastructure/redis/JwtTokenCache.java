package com.huawei.devbridge.relaycontroller.infrastructure.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenCache {
    private static final String KEY_PREFIX = "jwt:";
    private final StringRedisTemplate stringRedisTemplate;

    public String get(String tunnelId) {
        try {
            return stringRedisTemplate.opsForValue().get(KEY_PREFIX + tunnelId);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public void set(String tunnelId, String token, long ttlSeconds) {
        try {
            stringRedisTemplate.opsForValue().set(KEY_PREFIX + tunnelId, token, Duration.ofSeconds(ttlSeconds));
        } catch (RuntimeException ignored) {
            // Redis is a cache here; token generation remains functional if Redis is unavailable.
        }
    }

    public void delete(String tunnelId) {
        try {
            stringRedisTemplate.delete(KEY_PREFIX + tunnelId);
        } catch (RuntimeException ignored) {
        }
    }
}
