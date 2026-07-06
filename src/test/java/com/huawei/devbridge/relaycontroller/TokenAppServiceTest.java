package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.application.service.LocalGridService;
import com.huawei.devbridge.relaycontroller.application.service.TokenAppService;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.domain.model.Grid;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.repository.GridRepository;
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
    @Mock
    private GridRepository gridRepository;

    @Test
    void createTokenChecksNamespaceWhenUserIdExists() {
        TokenAppService service = newService();
        CreateTokenRequest request = new CreateTokenRequest();
        request.setTunnelId("000001e240");

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel());
        when(gridRepository.findByGridNameAndRegion("grid-a", "region-a"))
                .thenReturn(Grid.builder().grid("grid-a").region("region-a").build());
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
    void createTokenRejectsGridOutsideLocalRegion() {
        TokenAppService service = newService();
        CreateTokenRequest request = new CreateTokenRequest();
        request.setTunnelId("000001e240");

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel());

        assertThatThrownBy(() -> service.createToken("ns-user-001", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GRID_NOT_FOUND);
    }

    private TokenAppService newService() {
        RelayProperties properties = new RelayProperties();
        return new TokenAppService(
                tunnelRepository,
                jwtTokenService,
                new LocalGridService(gridRepository, properties),
                new NamespaceService(),
                new TunnelDomainService(),
                properties);
    }

    private Tunnel tunnel() {
        return Tunnel.builder()
                .tunnelId("000001e240")
                .tunnelCode(123456L)
                .gridName("grid-a")
                .namespace("ns-user-001")
                .deleted(0)
                .build();
    }
}
