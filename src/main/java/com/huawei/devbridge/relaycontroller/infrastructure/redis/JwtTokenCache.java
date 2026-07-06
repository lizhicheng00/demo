package com.huawei.devbridge.relaycontroller.infrastructure.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenCache {
    private static final String RT_KEY_PREFIX = "jwt:rt:";

    private final StringRedisTemplate stringRedisTemplate;

    public String getReusableToken(String tunnelId) {
        try {
            return stringRedisTemplate.opsForValue().get(RT_KEY_PREFIX + tunnelId);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public void setReusableToken(String tunnelId, String token, long ttlSeconds) {
        try {
            stringRedisTemplate.opsForValue().set(RT_KEY_PREFIX + tunnelId, token, Duration.ofSeconds(ttlSeconds));
        } catch (RuntimeException ignored) {
            // Redis is a cache here; token generation remains functional if Redis is unavailable.
        }
    }

    public void deleteReusableToken(String tunnelId) {
        try {
            stringRedisTemplate.delete(RT_KEY_PREFIX + tunnelId);
        } catch (RuntimeException ignored) {
        }
    }
}
