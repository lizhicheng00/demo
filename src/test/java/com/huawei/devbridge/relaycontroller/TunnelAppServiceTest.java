package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.application.service.LocalGridService;
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
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelListItemResponse;
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
                new LocalGridService(gridRepository, properties),
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

        when(gridRepository.findByGridNameAndRegion("grid-a", "region-a"))
                .thenReturn(Grid.builder().grid("grid-a").region("region-a").build());
        when(tunnelRepository.existsByTunnelCode(123456L)).thenReturn(false);
        when(tunnelRepository.existsByTunnelId("aaaadysa")).thenReturn(false);
        when(tunnelRepository.save(org.mockito.ArgumentMatchers.any(Tunnel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateTunnelResponse response = service.createTunnel("ns-user-001", request);
        long after = TimeUtils.nowSeconds();

        assertThat(response.getTunnelId()).isEqualTo("aaaadysa");
        assertThat(response.getTunnelCode()).isEqualTo(123456L);
        assertThat(response.getUrl()).isEqualTo("aaaadysa-grid-a-myhuaweicloud.com");
        assertThat(response.getType()).isEqualTo("bridge");
        assertThat(response.getExpiration())
                .isBetween(Math.toIntExact(before + 72 * 3600L), Math.toIntExact(after + 72 * 3600L));
    }

    @Test
    void createTunnelRejectsGridOutsideLocalRegion() {
        TunnelAppService service = newService(new RelayProperties());
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setGridName("grid-b");

        assertThatThrownBy(() -> service.createTunnel("ns-user-001", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GRID_NOT_FOUND);
    }

    @Test
    void createTunnelUsesCustomExpirationHours() {
        TunnelAppService service = newService(new RelayProperties());
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setGridName("grid-a");
        request.setExpiration(2);
        long before = TimeUtils.nowSeconds();

        when(gridRepository.findByGridNameAndRegion("grid-a", "region-a"))
                .thenReturn(Grid.builder().grid("grid-a").region("region-a").build());
        when(tunnelRepository.existsByTunnelCode(123456L)).thenReturn(false);
        when(tunnelRepository.existsByTunnelId("aaaadysa")).thenReturn(false);
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

        when(gridRepository.findByGridNameAndRegion("grid-a", "region-a"))
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
        request.setExpiration(1);
        Tunnel tunnel = Tunnel.builder()
                .tunnelId("aaaadysa")
                .namespace("ns-user-001")
                .gridName("grid-a")
                .deleted(0)
                .expiration(Math.toIntExact(TimeUtils.nowSeconds() + 1800))
                .build();

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel);

        long before = TimeUtils.nowSeconds();
        Boolean updated = service.updateTunnel("ns-user-001", "aaaadysa", request);
        long after = TimeUtils.nowSeconds();

        assertThat(updated).isTrue();
        assertThat(tunnel.getExpiration())
                .isBetween(Math.toIntExact(before + 3600L), Math.toIntExact(after + 3600L));
        verify(tunnelRepository).update(tunnel);
        verify(jwtTokenService).evictToken("aaaadysa");
    }

    @Test
    void listTunnelsQueriesLocalRegionOnly() {
        TunnelAppService service = newService(new RelayProperties());
        Tunnel local = Tunnel.builder()
                .name("local")
                .namespace("ns-user-001")
                .gridName("grid-a")
                .url("local-grid-a-myhuaweicloud.com")
                .deleted(0)
                .build();

        when(tunnelRepository.findActiveByNamespaceAndRegion(
                eq("ns-user-001"), eq(null), eq("region-a"), anyLong()))
                .thenReturn(List.of(local));

        List<TunnelListItemResponse> response = service.listTunnels("ns-user-001", null);

        assertThat(response).extracting(TunnelListItemResponse::getName).containsExactly("local");
    }

    @Test
    void listTunnelsQueriesActiveTunnelsOnly() {
        TunnelAppService service = newService(new RelayProperties());

        when(tunnelRepository.findActiveByNamespaceAndRegion(
                eq("ns-user-001"), eq(null), eq("region-a"), anyLong()))
                .thenReturn(List.of());

        List<TunnelListItemResponse> response = service.listTunnels("ns-user-001", null);

        assertThat(response).isEmpty();
    }

    @Test
    void updateTunnelStoresEnumType() {
        TunnelAppService service = newService(new RelayProperties());
        UpdateTunnelRequest request = new UpdateTunnelRequest();
        request.setType(TunnelType.ENV);
        Tunnel tunnel = Tunnel.builder()
                .tunnelId("aaaadysa")
                .namespace("ns-user-001")
                .gridName("grid-a")
                .deleted(0)
                .type(TunnelType.BRIDGE)
                .build();

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel);

        Boolean updated = service.updateTunnel("ns-user-001", "aaaadysa", request);

        assertThat(updated).isTrue();
        assertThat(tunnel.getType()).isEqualTo(TunnelType.ENV);
        verify(tunnelRepository).update(tunnel);
    }

    @Test
    void deleteTunnelCleansTunnelPorts() {
        TunnelAppService service = newService(new RelayProperties());
        Tunnel tunnel = Tunnel.builder()
                .tunnelId("aaaadysa")
                .tunnelCode(123456L)
                .namespace("ns-user-001")
                .gridName("grid-a")
                .deleted(0)
                .build();

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel);

        Boolean deleted = service.deleteTunnel("ns-user-001", "aaaadysa");

        assertThat(deleted).isTrue();
        verify(tunnelRepository).softDelete(eq("aaaadysa"), anyLong());
        verify(jwtTokenService).evictToken("aaaadysa");
        verify(tunnelPortRepository).deleteByTunnelCode(123456L);
    }

    @Test
    void deleteTunnelsCleansLocalRegionTunnelsOnly() {
        TunnelAppService service = newService(new RelayProperties());
        Tunnel first = Tunnel.builder()
                .tunnelId("aaaadysa")
                .tunnelCode(123456L)
                .namespace("ns-user-001")
                .gridName("grid-a")
                .deleted(0)
                .build();

        when(tunnelRepository.findByNamespaceAndRegion("ns-user-001", "region-a")).thenReturn(List.of(first));

        Boolean deleted = service.deleteTunnels("ns-user-001");

        assertThat(deleted).isTrue();
        verify(tunnelRepository).softDelete(eq("aaaadysa"), anyLong());
        verify(jwtTokenService).evictToken("aaaadysa");
        verify(tunnelPortRepository).deleteByTunnelCode(123456L);
    }

    private TunnelAppService newService(RelayProperties properties) {
        return new TunnelAppService(
                tunnelRepository,
                new LocalGridService(gridRepository, properties),
                new NamespaceService(),
                new FixedTunnelCodeGenerator(),
                jwtTokenService,
                new TunnelDomainService(),
                tunnelPortRepository,
                properties);
    }

    private void stubLocalGrid(String gridName) {
        when(gridRepository.findByGridNameAndRegion(gridName, "region-a"))
                .thenReturn(Grid.builder().grid(gridName).region("region-a").build());
    }

    private static class FixedTunnelCodeGenerator extends TunnelCodeGenerator {
        @Override
        public long generate() {
            return 123456L;
        }
    }
}
