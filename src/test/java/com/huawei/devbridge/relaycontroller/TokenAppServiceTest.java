package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.application.service.TokenAppService;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.domain.model.OttClaims;
import com.huawei.devbridge.relaycontroller.domain.model.OttConsumeResult;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.domain.service.JwtTokenService;
import com.huawei.devbridge.relaycontroller.domain.service.NamespaceService;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelDomainService;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateOttTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateRtTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateOttTokenResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateRtTokenResponse;
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
    void createOttReturnsOneTimeToken() {
        TokenAppService service = newService();
        CreateOttTokenRequest request = new CreateOttTokenRequest();
        request.setTunnelId("000001e240");
        request.setGridName("grid-a");
        request.setConnId("conn-1");

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel());
        when(jwtTokenService.createOneTimeToken(ArgumentMatchers.argThat(command ->
                command.getTunnel().getTunnelId().equals("000001e240")
                        && command.getJti().startsWith("ott:000001e240:conn-1:"))))
                .thenReturn("ott-token");

        CreateOttTokenResponse response = service.createOtt(request);

        assertThat(response.getTokenType()).isEqualTo("OTT");
        assertThat(response.getToken()).isEqualTo("ott-token");
        assertThat(response.getExpiresIn()).isEqualTo(1800L);
    }

    @Test
    void createRtDirectChecksNamespaceWhenUserIdExists() {
        TokenAppService service = newService();
        CreateRtTokenRequest request = new CreateRtTokenRequest();
        request.setTunnelId("000001e240");

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel());
        when(jwtTokenService.getOrCreateReusableToken(ArgumentMatchers.any(Tunnel.class))).thenReturn("rt-token");

        CreateRtTokenResponse response = service.createRt(null, "user-001", request);

        assertThat(response.getTokenType()).isEqualTo("RT");
        assertThat(response.getToken()).isEqualTo("rt-token");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);
    }

    @Test
    void createRtWithOttRejectsConsumedToken() {
        TokenAppService service = newService();
        when(jwtTokenService.parseAndVerifyOtt("ott-token")).thenReturn(OttClaims.builder()
                .jti("ott:000001e240:conn-1:test")
                .tunnelId("000001e240")
                .build());
        when(jwtTokenService.consumeOneTimeToken("ott:000001e240:conn-1:test"))
                .thenReturn(OttConsumeResult.ALREADY_CONSUMED);

        assertThatThrownBy(() -> service.createRt("Bearer ott-token", null, new CreateRtTokenRequest()))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOKEN_ALREADY_CONSUMED);
    }

    private TokenAppService newService() {
        return new TokenAppService(
                tunnelRepository,
                jwtTokenService,
                new NamespaceService(),
                new TunnelDomainService(),
                new RelayProperties());
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
