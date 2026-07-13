package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.domain.model.JwtScope;
import com.huawei.devbridge.relaycontroller.domain.model.JwtToken;
import com.huawei.devbridge.relaycontroller.domain.model.JwtTokens;
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
    void getOrCreateTokensReturnsBothCachedValues() {
        JwtTokenServiceImpl service = newService(new RelayProperties());
        Tunnel tunnel = Tunnel.builder().tunnelId("aaaadysa").build();
        when(jwtTokenCache.getToken("aaaadysa", JwtScope.CONNECT))
                .thenReturn(new JwtToken("cached-connect", 3600L));
        when(jwtTokenCache.getToken("aaaadysa", JwtScope.HOST))
                .thenReturn(new JwtToken("cached-host", 3599L));

        JwtTokens tokens = service.getOrCreateTokens(tunnel);

        assertThat(tokens.connect()).isEqualTo("cached-connect");
        assertThat(tokens.host()).isEqualTo("cached-host");
        assertThat(tokens.expiresIn()).isEqualTo(3599L);
        verify(jwtSigner, never()).signToken(eq(tunnel), eq(JwtScope.CONNECT), anyLong());
    }

    @Test
    void getOrCreateTokensCreatesAndCachesBothScopes() {
        RelayProperties properties = new RelayProperties();
        properties.getJwt().getToken().setTtlSeconds(86400);
        JwtTokenServiceImpl service = newService(properties);
        Tunnel tunnel = Tunnel.builder().tunnelId("aaaadysa").build();
        when(jwtSigner.signToken(tunnel, JwtScope.CONNECT, 86400L)).thenReturn("new-connect");
        when(jwtSigner.signToken(tunnel, JwtScope.HOST, 86400L)).thenReturn("new-host");

        JwtTokens tokens = service.getOrCreateTokens(tunnel);

        assertThat(tokens.connect()).isEqualTo("new-connect");
        assertThat(tokens.host()).isEqualTo("new-host");
        assertThat(tokens.expiresIn()).isEqualTo(86400L);
        verify(jwtTokenCache).setToken("aaaadysa", JwtScope.CONNECT, "new-connect", 86400L);
        verify(jwtTokenCache).setToken("aaaadysa", JwtScope.HOST, "new-host", 86400L);
    }

    @Test
    void getOrCreateTokensCapsTtlByTunnelExpiration() {
        RelayProperties properties = new RelayProperties();
        properties.getJwt().getToken().setTtlSeconds(86400);
        JwtTokenServiceImpl service = newService(properties);
        Tunnel tunnel = Tunnel.builder()
                .tunnelId("aaaadysa")
                .expiration(Math.toIntExact(TimeUtils.nowSeconds() + 60))
                .build();
        when(jwtSigner.signToken(eq(tunnel), eq(JwtScope.CONNECT), anyLong())).thenReturn("new-connect");
        when(jwtSigner.signToken(eq(tunnel), eq(JwtScope.HOST), anyLong())).thenReturn("new-host");

        JwtTokens tokens = service.getOrCreateTokens(tunnel);

        assertThat(tokens.expiresIn()).isBetween(1L, 60L);
        verify(jwtTokenCache).setToken(eq("aaaadysa"), eq(JwtScope.CONNECT), eq("new-connect"),
                org.mockito.ArgumentMatchers.longThat(ttl -> ttl > 0 && ttl <= 60));
        verify(jwtTokenCache).setToken(eq("aaaadysa"), eq(JwtScope.HOST), eq("new-host"),
                org.mockito.ArgumentMatchers.longThat(ttl -> ttl > 0 && ttl <= 60));
    }

    @Test
    void getOrCreateTokensRotatesBothWhenOneCachedTokenIsMissing() {
        JwtTokenServiceImpl service = newService(new RelayProperties());
        Tunnel tunnel = Tunnel.builder().tunnelId("aaaadysa").build();
        when(jwtTokenCache.getToken("aaaadysa", JwtScope.CONNECT))
                .thenReturn(new JwtToken("stale-connect", 3600L));
        when(jwtSigner.signToken(tunnel, JwtScope.CONNECT, 86400L)).thenReturn("new-connect");
        when(jwtSigner.signToken(tunnel, JwtScope.HOST, 86400L)).thenReturn("new-host");

        JwtTokens tokens = service.getOrCreateTokens(tunnel);

        assertThat(tokens.connect()).isEqualTo("new-connect");
        assertThat(tokens.host()).isEqualTo("new-host");
    }

    private JwtTokenServiceImpl newService(RelayProperties properties) {
        return new JwtTokenServiceImpl(jwtTokenCache, jwtSigner, properties);
    }
}
