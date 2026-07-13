package com.huawei.devbridge.relaycontroller.infrastructure.redis;

import com.huawei.cloudspace.commons.framework.utils.ExceptionUtils;
import com.huawei.clouds.wushan.scc.crypto.SccCrypto;
import com.huawei.devbridge.relaycontroller.domain.model.JwtScope;
import com.huawei.devbridge.relaycontroller.domain.model.JwtToken;
import java.time.Duration;
import java.util.List;
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
    public JwtToken getToken(String tunnelId, JwtScope scope) {
        try {
            String key = tokenKey(tunnelId, scope);
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

    public void setToken(String tunnelId, JwtScope scope, String token, long ttlSeconds) {
        try {
            stringRedisTemplate.opsForValue().set(tokenKey(tunnelId, scope),
                    sccCrypto.encrypt(token), Duration.ofSeconds(ttlSeconds));
        } catch (RuntimeException exception) {
            log.warn("Failed to set jwt token cache: tunnelId={}, scope={}, error={}",
                    tunnelId, scope.value(), ExceptionUtils.anonymousMessage(exception));
        }
    }

    public void deleteToken(String tunnelId) {
        try {
            stringRedisTemplate.delete(List.of(
                    tokenKey(tunnelId, JwtScope.CONNECT), tokenKey(tunnelId, JwtScope.HOST)));
        } catch (RuntimeException exception) {
            log.warn("Failed to delete jwt token cache: tunnelId={}, error={}",
                    tunnelId, ExceptionUtils.anonymousMessage(exception));
        }
    }

    private static String tokenKey(String tunnelId, JwtScope scope) {
        return TOKEN_KEY_PREFIX + tunnelId + ":" + scope.value();
    }
}
