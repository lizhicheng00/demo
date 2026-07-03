package com.huawei.devbridge.relaycontroller.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.devbridge.relaycontroller.domain.model.OttClaims;
import com.huawei.devbridge.relaycontroller.domain.model.OttConsumeResult;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenCache {
    private static final String OTT_KEY_PREFIX = "jwt:ott:";
    private static final String RT_KEY_PREFIX = "jwt:rt:";
    private static final String UNUSED = "UNUSED";
    private static final String CONSUMED = "CONSUMED";
    private static final DefaultRedisScript<String> CONSUME_OTT_SCRIPT = new DefaultRedisScript<>("""
            local value = redis.call('GET', KEYS[1])
            if not value then
                return 'NOT_FOUND_OR_EXPIRED'
            end
            if string.find(value, '"status":"CONSUMED"', 1, true) then
                return 'ALREADY_CONSUMED'
            end
            if string.find(value, '"status":"UNUSED"', 1, true) then
                redis.call('SET', KEYS[1], '{"status":"CONSUMED"}', 'EX', ARGV[1])
                return 'CONSUMED'
            end
            return 'NOT_FOUND_OR_EXPIRED'
            """, String.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

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

    public void setOneTimeToken(OttClaims claims, long ttlSeconds) {
        stringRedisTemplate.opsForValue().set(OTT_KEY_PREFIX + claims.getJti(), toOttStatusJson(claims), Duration.ofSeconds(ttlSeconds));
    }

    public OttConsumeResult consumeOneTimeToken(String jti, long consumedTtlSeconds) {
        try {
            String result = stringRedisTemplate.execute(CONSUME_OTT_SCRIPT, List.of(OTT_KEY_PREFIX + jti), String.valueOf(consumedTtlSeconds));
            if (result == null) {
                return OttConsumeResult.NOT_FOUND_OR_EXPIRED;
            }
            return OttConsumeResult.valueOf(result);
        } catch (RuntimeException exception) {
            return OttConsumeResult.NOT_FOUND_OR_EXPIRED;
        }
    }

    public void deleteReusableToken(String tunnelId) {
        try {
            stringRedisTemplate.delete(RT_KEY_PREFIX + tunnelId);
        } catch (RuntimeException ignored) {
        }
    }

    private String toOttStatusJson(OttClaims claims) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("status", UNUSED);
        value.put("tunnelId", claims.getTunnelId());
        value.put("gridname", claims.getGridName());
        value.put("connId", claims.getConnId());
        value.put("exp", claims.getExpiresAt());
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{\"status\":\"" + UNUSED + "\"}";
        }
    }
}
