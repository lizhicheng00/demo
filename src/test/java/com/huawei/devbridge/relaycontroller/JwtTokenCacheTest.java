package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.domain.model.JwtScope;
import com.huawei.devbridge.relaycontroller.domain.model.JwtToken;
import com.huawei.devbridge.relaycontroller.infrastructure.redis.JwtTokenCache;
import com.huawei.devbridge.relaycontroller.infrastructure.security.SccCrypto;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class JwtTokenCacheTest {
    private static final String KEY = "jwt:token:aaaadysa:connect";

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private SccCrypto sccCrypto;
    @InjectMocks
    private JwtTokenCache cache;

    @Test
    void storesAndReadsScopedToken() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(sccCrypto.encrypt("jwt-token")).thenReturn("encrypted-token");

        cache.setToken("aaaadysa", JwtScope.CONNECT, "jwt-token", 60);

        verify(valueOperations).set(KEY, "encrypted-token", Duration.ofSeconds(60));

        when(valueOperations.get(KEY)).thenReturn("encrypted-token");
        when(redisTemplate.getExpire(KEY, TimeUnit.SECONDS)).thenReturn(60L);
        when(sccCrypto.decrypt("encrypted-token")).thenReturn("jwt-token");

        JwtToken token = cache.getToken("aaaadysa", JwtScope.CONNECT);

        assertThat(token.token()).isEqualTo("jwt-token");
        assertThat(token.expiresIn()).isEqualTo(60);
    }
}
