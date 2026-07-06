package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.application.service.LocalGridService;
import com.huawei.devbridge.relaycontroller.application.service.RelayStatusAppService;
import com.huawei.devbridge.relaycontroller.domain.model.Grid;
import com.huawei.devbridge.relaycontroller.domain.model.RelayStatus;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.repository.GridRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.RelayStatusRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.domain.service.NamespaceService;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelDomainService;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.interfaces.response.RelayStatusResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelayStatusAppServiceTest {
    @Mock
    private TunnelRepository tunnelRepository;
    @Mock
    private RelayStatusRepository relayStatusRepository;
    @Mock
    private GridRepository gridRepository;

    @Test
    void statusIsOfflineWithoutRuntimeRecordAndDoesNotFakeHeartbeat() {
        RelayStatusAppService service = newService();
        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(Tunnel.builder()
                .tunnelId("000001e240")
                .namespace("ns-user-001")
                .gridName("grid-a")
                .deleted(0)
                .build());
        when(gridRepository.findByGridNameAndRegion("grid-a", "region-a"))
                .thenReturn(Grid.builder().grid("grid-a").region("region-a").build());
        when(relayStatusRepository.findByTunnelId("000001e240")).thenReturn(null);

        RelayStatusResponse response = service.getStatus("ns-user-001", "000001e240");

        assertThat(response.getStatus()).isEqualTo("OFFLINE");
        assertThat(response.getGridName()).isEqualTo("grid-a");
        assertThat(response.getLastHeartbeat()).isNull();
    }

    @Test
    void statusIsOfflineWhenRuntimeGridDoesNotMatchTunnelGrid() {
        RelayStatusAppService service = newService();
        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(Tunnel.builder()
                .tunnelId("000001e240")
                .namespace("ns-user-001")
                .gridName("grid-a")
                .deleted(0)
                .build());
        when(gridRepository.findByGridNameAndRegion("grid-a", "region-a"))
                .thenReturn(Grid.builder().grid("grid-a").region("region-a").build());
        when(relayStatusRepository.findByTunnelId("000001e240")).thenReturn(RelayStatus.builder()
                .tunnelId("000001e240")
                .gridName("grid-b")
                .status("ONLINE")
                .lastHeartbeat(1720000000L)
                .build());

        RelayStatusResponse response = service.getStatus("ns-user-001", "000001e240");

        assertThat(response.getStatus()).isEqualTo("OFFLINE");
        assertThat(response.getGridName()).isEqualTo("grid-a");
        assertThat(response.getLastHeartbeat()).isNull();
    }

    private RelayStatusAppService newService() {
        RelayProperties properties = new RelayProperties();
        return new RelayStatusAppService(
                tunnelRepository,
                relayStatusRepository,
                new LocalGridService(gridRepository, properties),
                new NamespaceService(),
                new TunnelDomainService());
    }
}
