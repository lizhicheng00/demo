package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.application.service.TokenAppService;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.domain.service.JwtTokenService;
import com.huawei.devbridge.relaycontroller.domain.service.NamespaceService;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelDomainService;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateTokenResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenAppServiceTest {
    @Mock
    private TunnelRepository tunnelRepository;
    @Mock
    private JwtTokenService jwtTokenService;
    @Test
    void createTokenChecksNamespaceWhenUserIdExists() {
        TokenAppService service = newService();
        CreateTokenRequest request = new CreateTokenRequest();
        request.setTunnelId("aaaadysa");

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel());
        when(jwtTokenService.getTokenTtlSeconds(ArgumentMatchers.any(Tunnel.class))).thenReturn(86400L);
        when(jwtTokenService.getOrCreateToken(ArgumentMatchers.any(Tunnel.class))).thenReturn("token-token");

        CreateTokenResponse response = service.createToken("ns-user-001", request);

        assertThat(response.getTokenType()).isEqualTo("TOKEN");
        assertThat(response.getToken()).isEqualTo("token-token");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);
    }

    @Test
    void createTokenRejectsMissingTunnelId() {
        TokenAppService service = newService();

        assertThatThrownBy(() -> service.createToken(null, new CreateTokenRequest()))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARAM_INVALID);
    }

    @Test
    void createTokenRejectsTunnelOutsideLocalRegion() {
        TokenAppService service = newService();
        CreateTokenRequest request = new CreateTokenRequest();
        request.setTunnelId("aaaadysa");

        assertThatThrownBy(() -> service.createToken("ns-user-001", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_NOT_FOUND);
    }

    @Test
    void createTokenCapsExpiresInByTunnelExpiration() {
        TokenAppService service = newService();
        CreateTokenRequest request = new CreateTokenRequest();
        request.setTunnelId("aaaadysa");
        Tunnel tunnel = tunnel();
        tunnel.setExpiration(Math.toIntExact(TimeUtils.nowSeconds() + 60));

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel);
        when(jwtTokenService.getTokenTtlSeconds(ArgumentMatchers.any(Tunnel.class))).thenReturn(60L);
        when(jwtTokenService.getOrCreateToken(ArgumentMatchers.any(Tunnel.class))).thenReturn("token-token");

        CreateTokenResponse response = service.createToken("ns-user-001", request);

        assertThat(response.getExpiresIn()).isEqualTo(60L);
    }

    private TokenAppService newService() {
        RelayProperties properties = new RelayProperties();
        return new TokenAppService(
                tunnelRepository,
                jwtTokenService,
                new NamespaceService(),
                new TunnelDomainService(),
                properties);
    }

    private Tunnel tunnel() {
        return Tunnel.builder()
                .tunnelId("aaaadysa")
                .tunnelCode(123456L)
                .gridName("grid-a")
                .namespace("ns-user-001")
                .deleted(0)
                .build();
    }
}
