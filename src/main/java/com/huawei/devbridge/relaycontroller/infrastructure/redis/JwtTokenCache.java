package com.huawei.devbridge.relaycontroller.infrastructure.redis;

import com.huawei.devbridge.relaycontroller.common.util.ExceptionUtils;
import com.huawei.devbridge.relaycontroller.domain.model.JwtToken;
import com.huawei.devbridge.relaycontroller.infrastructure.security.SccCrypto;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenCache {
    private static final String TOKEN_KEY_PREFIX = "jwt:token:";

    private final StringRedisTemplate stringRedisTemplate;
    private final SccCrypto sccCrypto;

    @Nullable
    public JwtToken getToken(String tunnelId) {
        try {
            String key = TOKEN_KEY_PREFIX + tunnelId;
            String token = stringRedisTemplate.opsForValue().get(key);
            if (token == null || token.isBlank()) {
                return null;
            }
            long ttlSeconds = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttlSeconds <= 0) {
                return null;
            }
            return new JwtToken(sccCrypto.decrypt(token), ttlSeconds);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public void setToken(String tunnelId, String token, long ttlSeconds) {
        try {
            stringRedisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + tunnelId,
                    sccCrypto.encrypt(token), Duration.ofSeconds(ttlSeconds));
        } catch (RuntimeException exception) {
            log.warn("Failed to get jwt token cache: tunnelId={}, error={}",
                    tunnelId, ExceptionUtils.anonymousMessage(exception));        }
    }

    public void deleteToken(String tunnelId) {
        try {
            stringRedisTemplate.delete(TOKEN_KEY_PREFIX + tunnelId);
        } catch (RuntimeException exception) {
            log.warn("Failed to delete jwt token cache: tunnelId={}, error={}",
                    tunnelId, ExceptionUtils.anonymousMessage(exception));
        }
    }
}
