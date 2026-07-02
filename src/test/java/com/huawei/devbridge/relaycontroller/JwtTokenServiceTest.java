package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        verify(jwtSigner, never()).sign(tunnel);
    }
}
