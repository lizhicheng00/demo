package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.domain.model.JwtToken;
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
    void shouldStoreAndReadTokenThroughSccCrypto() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        SccCrypto crypto = new SccCrypto();
        JwtTokenCache cache = new JwtTokenCache(redisTemplate, crypto);

        cache.setToken("aaaadysa", "jwt-token", 60);

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq("jwt:token:aaaadysa"), valueCaptor.capture(), eq(Duration.ofSeconds(60)));
        assertThat(valueCaptor.getValue()).isEqualTo("jwt-token");

        when(valueOperations.get("jwt:token:aaaadysa")).thenReturn("{scc}jwt-token");
        when(redisTemplate.getExpire("jwt:token:aaaadysa", TimeUnit.SECONDS)).thenReturn(60L);

        JwtToken token = cache.getToken("aaaadysa");

        assertThat(token.token()).isEqualTo("jwt-token");
        assertThat(token.expiresIn()).isEqualTo(60);
    }
}
