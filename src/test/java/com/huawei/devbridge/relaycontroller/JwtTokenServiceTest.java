package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.domain.model.CreateOttTokenCommand;
import com.huawei.devbridge.relaycontroller.domain.model.OttClaims;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.infrastructure.redis.JwtTokenCache;
import com.huawei.devbridge.relaycontroller.infrastructure.security.JwtKeyProvider;
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
    @Mock
    private JwtKeyProvider jwtKeyProvider;

    @Test
    void getOrCreateReusableTokenReturnsCachedValue() {
        RelayProperties properties = new RelayProperties();
        JwtTokenServiceImpl service = new JwtTokenServiceImpl(jwtTokenCache, jwtSigner, jwtKeyProvider, properties);
        Tunnel tunnel = Tunnel.builder().tunnelId("000001e240").build();

        when(jwtTokenCache.getReusableToken("000001e240")).thenReturn("cached-token");

        String token = service.getOrCreateReusableToken(tunnel);

        assertThat(token).isEqualTo("cached-token");
        verify(jwtSigner, never()).signReusableToken(eq(tunnel), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void getOrCreateReusableTokenCreatesAndCachesRt() {
        RelayProperties properties = new RelayProperties();
        properties.getJwt().getRt().setTtlSeconds(86400);
        JwtTokenServiceImpl service = new JwtTokenServiceImpl(jwtTokenCache, jwtSigner, jwtKeyProvider, properties);
        Tunnel tunnel = Tunnel.builder().tunnelId("000001e240").build();

        when(jwtTokenCache.getReusableToken("000001e240")).thenReturn(null);
        when(jwtSigner.signReusableToken(eq(tunnel), org.mockito.ArgumentMatchers.startsWith("rt:000001e240:"), eq(86400L)))
                .thenReturn("new-token");

        String token = service.getOrCreateReusableToken(tunnel);

        assertThat(token).isEqualTo("new-token");
        verify(jwtTokenCache).setReusableToken("000001e240", "new-token", 86400L);
    }

    @Test
    void createOneTimeTokenWritesOttState() {
        RelayProperties properties = new RelayProperties();
        properties.getJwt().getOtt().setTtlSeconds(1800);
        JwtTokenServiceImpl service = new JwtTokenServiceImpl(jwtTokenCache, jwtSigner, jwtKeyProvider, properties);
        Tunnel tunnel = Tunnel.builder()
                .tunnelId("000001e240")
                .tunnelCode(123456L)
                .namespace("ns-user-001")
                .gridName("grid-a")
                .build();
        CreateOttTokenCommand command = CreateOttTokenCommand.builder()
                .jti("ott:000001e240:conn-1:test")
                .tunnel(tunnel)
                .connId("conn-1")
                .build();

        when(jwtSigner.signOneTimeToken(command, 1800L)).thenReturn("ott-token");

        String token = service.createOneTimeToken(command);

        assertThat(token).isEqualTo("ott-token");
        verify(jwtTokenCache).setOneTimeToken(org.mockito.ArgumentMatchers.argThat((OttClaims claims) ->
                claims.getJti().equals("ott:000001e240:conn-1:test")
                        && claims.getTunnelId().equals("000001e240")
                        && claims.getGridName().equals("grid-a")), eq(1800L));
    }
}
