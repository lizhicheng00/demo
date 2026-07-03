package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.infrastructure.redis.JwtTokenCache;
import com.huawei.devbridge.relaycontroller.infrastructure.security.JwtSigner;
import com.huawei.devbridge.relaycontroller.infrastructure.security.JwtTokenServiceImpl;
import com.huawei.devbridge.relaycontroller.infrastructure.security.PublicKeyConfigProvider;
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
    private PublicKeyConfigProvider publicKeyConfigProvider;

    @Test
    void getOrCreateTokenReturnsCachedValue() {
        RelayProperties properties = new RelayProperties();
        JwtTokenServiceImpl service = new JwtTokenServiceImpl(jwtTokenCache, jwtSigner, publicKeyConfigProvider, properties);
        Tunnel tunnel = Tunnel.builder().tunnelid("000001e240").build();

        when(jwtTokenCache.get("000001e240")).thenReturn("cached-token");

        String token = service.getOrCreateToken(tunnel);

        assertThat(token).isEqualTo("cached-token");
        verify(jwtSigner, never()).sign(eq(tunnel), anyLong());
    }

    @Test
    void getOrCreateTokenCapsCacheTtlAtTunnelExpiration() {
        RelayProperties properties = new RelayProperties();
        properties.getJwt().setTtlSeconds(86400);
        JwtTokenServiceImpl service = new JwtTokenServiceImpl(jwtTokenCache, jwtSigner, publicKeyConfigProvider, properties);
        Tunnel tunnel = Tunnel.builder()
                .tunnelid("000001e240")
                .expiration(Math.toIntExact(TimeUtils.nowSeconds() + 60))
                .build();

        when(jwtTokenCache.get("000001e240")).thenReturn(null);
        when(jwtSigner.sign(eq(tunnel), anyLong())).thenReturn("new-token");

        String token = service.getOrCreateToken(tunnel);

        assertThat(token).isEqualTo("new-token");
        verify(jwtSigner).sign(eq(tunnel), org.mockito.ArgumentMatchers.longThat(ttl -> ttl > 0 && ttl <= 60));
        verify(jwtTokenCache).set(eq("000001e240"), eq("new-token"), org.mockito.ArgumentMatchers.longThat(ttl -> ttl > 0 && ttl <= 60));
    }

    @Test
    void getOrCreateTokenRejectsExpiredTunnelBeforeReturningCachedToken() {
        RelayProperties properties = new RelayProperties();
        JwtTokenServiceImpl service = new JwtTokenServiceImpl(jwtTokenCache, jwtSigner, publicKeyConfigProvider, properties);
        Tunnel tunnel = Tunnel.builder()
                .tunnelid("000001e240")
                .expiration(Math.toIntExact(TimeUtils.nowSeconds() - 1))
                .build();

        assertThatThrownBy(() -> service.getOrCreateToken(tunnel))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_EXPIRED);

        verify(jwtTokenCache, never()).get("000001e240");
    }
}
