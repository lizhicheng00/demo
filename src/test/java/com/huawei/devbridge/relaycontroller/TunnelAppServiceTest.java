package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.application.assembler.TunnelAssembler;
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
    void createTunnelAllocatesCodeAndReturnsToken() {
        RelayProperties properties = new RelayProperties();
        TunnelAppService service = new TunnelAppService(
                tunnelRepository,
                gridRepository,
                new NamespaceService(),
                new FixedTunnelCodeGenerator(),
                jwtTokenService,
                new TunnelDomainService(),
                new TunnelAssembler(),
                properties);
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setGridname("grid-a");
        request.setCluster("cluster-a");

        when(gridRepository.findByGridName("grid-a"))
                .thenReturn(Grid.builder().grid("grid-a").region("region-a").build());
        when(tunnelRepository.existsByTunnelCode(123456L)).thenReturn(false);
        when(tunnelRepository.existsByTunnelId("000001e240")).thenReturn(false);
        when(tunnelRepository.save(any(Tunnel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.getOrCreateToken(any(Tunnel.class))).thenReturn("jwt-token");

        CreateTunnelResponse response = service.createTunnel("user-001", request);

        assertThat(response.getTunnelId()).isEqualTo("000001e240");
        assertThat(response.getTunnelCode()).isEqualTo(123456L);
        assertThat(response.getUrl()).isEqualTo("000001e240.region-a.relayprovider.xxx.com");
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
    }

    private static class FixedTunnelCodeGenerator extends TunnelCodeGenerator {
        @Override
        public long generate() {
            return 123456L;
        }
    }
}
