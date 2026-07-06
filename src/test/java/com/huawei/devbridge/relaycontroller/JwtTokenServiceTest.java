package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.infrastructure.redis.JwtTokenCache;
import com.huawei.devbridge.relaycontroller.infrastructure.security.JwtSigner;
import com.huawei.devbridge.relaycontroller.infrastructure.security.JwtTokenServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {
    @Mock
    private JwtTokenCache jwtTokenCache;
    @Mock
    private JwtSigner jwtSigner;

    @Test
    void getOrCreateTokenReturnsCachedValue() {
        RelayProperties properties = new RelayProperties();
        JwtTokenServiceImpl service = new JwtTokenServiceImpl(jwtTokenCache, jwtSigner, properties);
        Tunnel tunnel = Tunnel.builder().tunnelId("000001e240").build();

        when(jwtTokenCache.getToken("000001e240")).thenReturn("cached-token");

        String token = service.getOrCreateToken(tunnel);

        assertThat(token).isEqualTo("cached-token");
        verify(jwtSigner, never()).signToken(eq(tunnel), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void getOrCreateTokenCreatesAndCachesToken() {
        RelayProperties properties = new RelayProperties();
        properties.getJwt().getToken().setTtlSeconds(86400);
        JwtTokenServiceImpl service = new JwtTokenServiceImpl(jwtTokenCache, jwtSigner, properties);
        Tunnel tunnel = Tunnel.builder().tunnelId("000001e240").build();

        when(jwtTokenCache.getToken("000001e240")).thenReturn(null);
        when(jwtSigner.signToken(eq(tunnel), org.mockito.ArgumentMatchers.startsWith("token:000001e240:"), eq(86400L)))
                .thenReturn("new-token");

        String token = service.getOrCreateToken(tunnel);

        assertThat(token).isEqualTo("new-token");
        verify(jwtTokenCache).setToken("000001e240", "new-token", 86400L);
    }
}
