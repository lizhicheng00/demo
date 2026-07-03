package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.application.service.TunnelAppService;
import com.huawei.devbridge.relaycontroller.domain.model.Grid;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.repository.GridRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.domain.service.JwtTokenService;
import com.huawei.devbridge.relaycontroller.domain.service.NamespaceService;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelCodeGenerator;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelDomainService;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.UpdateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateTunnelResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TunnelAppServiceTest {
    @Mock
    private TunnelRepository tunnelRepository;
    @Mock
    private GridRepository gridRepository;
    @Mock
    private JwtTokenService jwtTokenService;

    @Test
    void createTunnelAllocatesCodeAndReturnsMetadata() {
        RelayProperties properties = new RelayProperties();
        TunnelAppService service = new TunnelAppService(
                tunnelRepository,
                gridRepository,
                new NamespaceService(),
                new FixedTunnelCodeGenerator(),
                jwtTokenService,
                new TunnelDomainService(),
                properties);
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setGridName("grid-a");
        request.setCluster("cluster-a");

        when(gridRepository.findByGridName("grid-a"))
                .thenReturn(Grid.builder().grid("grid-a").region("region-a").build());
        when(tunnelRepository.existsByTunnelCode(123456L)).thenReturn(false);
        when(tunnelRepository.existsByTunnelId("000001e240")).thenReturn(false);
        when(tunnelRepository.save(org.mockito.ArgumentMatchers.any(Tunnel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateTunnelResponse response = service.createTunnel("user-001", request);

        assertThat(response.getTunnelId()).isEqualTo("000001e240");
        assertThat(response.getTunnelCode()).isEqualTo(123456L);
        assertThat(response.getUrl()).isEqualTo("000001e240.region-a.relayprovider.xxx.com");
    }

    @Test
    void createTunnelRejectsPastExpiration() {
        TunnelAppService service = newService(new RelayProperties());
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setGridName("grid-a");
        request.setExpiration(Math.toIntExact(TimeUtils.nowSeconds() - 1));

        when(gridRepository.findByGridName("grid-a"))
                .thenReturn(Grid.builder().grid("grid-a").region("region-a").build());

        assertThatThrownBy(() -> service.createTunnel("user-001", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_EXPIRED);
    }

    @Test
    void updateTunnelEvictsTokenWhenExpirationChanges() {
        TunnelAppService service = newService(new RelayProperties());
        UpdateTunnelRequest request = new UpdateTunnelRequest();
        request.setTunnelId("000001e240");
        request.setExpiration(Math.toIntExact(TimeUtils.nowSeconds() + 3600));
        Tunnel tunnel = Tunnel.builder()
                .tunnelId("000001e240")
                .namespace("ns-user-001")
                .deleted(0)
                .expiration(Math.toIntExact(TimeUtils.nowSeconds() + 1800))
                .build();

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel);

        Boolean updated = service.updateTunnel("user-001", request);

        assertThat(updated).isTrue();
        verify(tunnelRepository).update(tunnel);
        verify(jwtTokenService).evictReusableToken("000001e240");
    }

    private TunnelAppService newService(RelayProperties properties) {
        return new TunnelAppService(
                tunnelRepository,
                gridRepository,
                new NamespaceService(),
                new FixedTunnelCodeGenerator(),
                jwtTokenService,
                new TunnelDomainService(),
                properties);
    }

    private static class FixedTunnelCodeGenerator extends TunnelCodeGenerator {
        @Override
        public long generate() {
            return 123456L;
        }
    }
}
