package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.application.service.TunnelAppService;
import com.huawei.devbridge.relaycontroller.domain.model.Grid;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.model.TunnelType;
import com.huawei.devbridge.relaycontroller.domain.repository.GridRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelPortRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.domain.service.JwtTokenService;
import com.huawei.devbridge.relaycontroller.domain.service.NamespaceService;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelCodeGenerator;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelDomainService;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.UpdateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateTunnelResponse;
import java.util.List;
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
    @Mock
    private TunnelPortRepository tunnelPortRepository;

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
                tunnelPortRepository,
                properties);
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setGridName("grid-a");
        request.setCluster("cluster-a");
        long before = TimeUtils.nowSeconds();

        when(gridRepository.findByGridName("grid-a"))
                .thenReturn(Grid.builder().grid("grid-a").region("region-a").build());
        when(tunnelRepository.existsByTunnelCode(123456L)).thenReturn(false);
        when(tunnelRepository.existsByTunnelId("000001e240")).thenReturn(false);
        when(tunnelRepository.save(org.mockito.ArgumentMatchers.any(Tunnel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateTunnelResponse response = service.createTunnel("ns-user-001", request);
        long after = TimeUtils.nowSeconds();

        assertThat(response.getTunnelId()).isEqualTo("000001e240");
        assertThat(response.getTunnelCode()).isEqualTo(123456L);
        assertThat(response.getUrl()).isEqualTo("000001e240.region-a.relayprovider.xxx.com");
        assertThat(response.getType()).isEqualTo("bridge");
        assertThat(response.getExpiration())
                .isBetween(Math.toIntExact(before + 72 * 3600L), Math.toIntExact(after + 72 * 3600L));
    }

    @Test
    void createTunnelUsesCustomExpirationHours() {
        TunnelAppService service = newService(new RelayProperties());
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setGridName("grid-a");
        request.setExpiration(2);
        long before = TimeUtils.nowSeconds();

        when(gridRepository.findByGridName("grid-a"))
                .thenReturn(Grid.builder().grid("grid-a").region("region-a").build());
        when(tunnelRepository.existsByTunnelCode(123456L)).thenReturn(false);
        when(tunnelRepository.existsByTunnelId("000001e240")).thenReturn(false);
        when(tunnelRepository.save(org.mockito.ArgumentMatchers.any(Tunnel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateTunnelResponse response = service.createTunnel("ns-user-001", request);
        long after = TimeUtils.nowSeconds();

        assertThat(response.getExpiration())
                .isBetween(Math.toIntExact(before + 2 * 3600L), Math.toIntExact(after + 2 * 3600L));
    }

    @Test
    void createTunnelRejectsNonPositiveExpirationHours() {
        TunnelAppService service = newService(new RelayProperties());
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setGridName("grid-a");
        request.setExpiration(0);

        when(gridRepository.findByGridName("grid-a"))
                .thenReturn(Grid.builder().grid("grid-a").region("region-a").build());

        assertThatThrownBy(() -> service.createTunnel("ns-user-001", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARAM_INVALID);
    }

    @Test
    void updateTunnelEvictsTokenWhenExpirationChanges() {
        TunnelAppService service = newService(new RelayProperties());
        UpdateTunnelRequest request = new UpdateTunnelRequest();
        request.setTunnelId("000001e240");
        request.setExpiration(1);
        Tunnel tunnel = Tunnel.builder()
                .tunnelId("000001e240")
                .namespace("ns-user-001")
                .deleted(0)
                .expiration(Math.toIntExact(TimeUtils.nowSeconds() + 1800))
                .build();

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel);

        long before = TimeUtils.nowSeconds();
        Boolean updated = service.updateTunnel("ns-user-001", request);
        long after = TimeUtils.nowSeconds();

        assertThat(updated).isTrue();
        assertThat(tunnel.getExpiration())
                .isBetween(Math.toIntExact(before + 3600L), Math.toIntExact(after + 3600L));
        verify(tunnelRepository).update(tunnel);
        verify(jwtTokenService).evictToken("000001e240");
    }

    @Test
    void updateTunnelStoresEnumType() {
        TunnelAppService service = newService(new RelayProperties());
        UpdateTunnelRequest request = new UpdateTunnelRequest();
        request.setTunnelId("000001e240");
        request.setType(TunnelType.ENV);
        Tunnel tunnel = Tunnel.builder()
                .tunnelId("000001e240")
                .namespace("ns-user-001")
                .deleted(0)
                .type(TunnelType.BRIDGE)
                .build();

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel);

        Boolean updated = service.updateTunnel("ns-user-001", request);

        assertThat(updated).isTrue();
        assertThat(tunnel.getType()).isEqualTo(TunnelType.ENV);
        verify(tunnelRepository).update(tunnel);
    }

    @Test
    void deleteTunnelCleansTunnelPorts() {
        TunnelAppService service = newService(new RelayProperties());
        Tunnel tunnel = Tunnel.builder()
                .tunnelId("000001e240")
                .tunnelCode(123456L)
                .namespace("ns-user-001")
                .deleted(0)
                .build();

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel);

        Boolean deleted = service.deleteTunnel("ns-user-001", "000001e240");

        assertThat(deleted).isTrue();
        verify(tunnelRepository).softDelete(eq("000001e240"), anyLong());
        verify(jwtTokenService).evictToken("000001e240");
        verify(tunnelPortRepository).deleteByTunnelCode(123456L);
    }

    @Test
    void deleteTunnelsCleansUserTunnels() {
        TunnelAppService service = newService(new RelayProperties());
        Tunnel first = Tunnel.builder()
                .tunnelId("000001e240")
                .tunnelCode(123456L)
                .namespace("ns-user-001")
                .deleted(0)
                .build();
        Tunnel second = Tunnel.builder()
                .tunnelId("000001e241")
                .tunnelCode(123457L)
                .namespace("ns-user-001")
                .deleted(0)
                .build();

        when(tunnelRepository.findByNamespace("ns-user-001", null)).thenReturn(List.of(first, second));

        Boolean deleted = service.deleteTunnels("ns-user-001");

        assertThat(deleted).isTrue();
        verify(tunnelRepository).softDeleteByNamespace(eq("ns-user-001"), anyLong());
        verify(jwtTokenService).evictToken("000001e240");
        verify(jwtTokenService).evictToken("000001e241");
        verify(tunnelPortRepository).deleteByTunnelCode(123456L);
        verify(tunnelPortRepository).deleteByTunnelCode(123457L);
    }

    private TunnelAppService newService(RelayProperties properties) {
        return new TunnelAppService(
                tunnelRepository,
                gridRepository,
                new NamespaceService(),
                new FixedTunnelCodeGenerator(),
                jwtTokenService,
                new TunnelDomainService(),
                tunnelPortRepository,
                properties);
    }

    private static class FixedTunnelCodeGenerator extends TunnelCodeGenerator {
        @Override
        public long generate() {
            return 123456L;
        }
    }
}
