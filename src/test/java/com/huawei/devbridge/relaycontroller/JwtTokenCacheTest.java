package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.domain.model.JwtToken;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.infrastructure.redis.JwtTokenCache;
import com.huawei.devbridge.relaycontroller.infrastructure.security.SccCrypto;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class JwtTokenCacheTest {

    @Test
    void shouldStoreEncryptedTokenAndReadPlainToken() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        RelayProperties properties = new RelayProperties();
        properties.getCrypto().setKey("test-key");
        SccCrypto crypto = new SccCrypto(properties);
        JwtTokenCache cache = new JwtTokenCache(redisTemplate, crypto);

        cache.setToken("aaaadysa", "jwt-token", 60);

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq("jwt:token:aaaadysa"), valueCaptor.capture(), eq(Duration.ofSeconds(60)));
        String encrypted = valueCaptor.getValue();
        assertThat(encrypted).startsWith("{scc}");

        when(valueOperations.get("jwt:token:aaaadysa")).thenReturn(encrypted);
        when(redisTemplate.getExpire("jwt:token:aaaadysa", TimeUnit.SECONDS)).thenReturn(60L);

        JwtToken token = cache.getToken("aaaadysa");

        assertThat(token.token()).isEqualTo("jwt-token");
        assertThat(token.expiresIn()).isEqualTo(60);
    }
}
