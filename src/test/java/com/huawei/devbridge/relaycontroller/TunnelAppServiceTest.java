package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.application.service.LocalClusterService;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.application.service.TunnelAppService;
import com.huawei.devbridge.relaycontroller.domain.model.Cluster;
import com.huawei.devbridge.relaycontroller.domain.model.JwtScope;
import com.huawei.devbridge.relaycontroller.domain.model.JwtToken;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.model.TunnelType;
import com.huawei.devbridge.relaycontroller.domain.repository.ClusterRepository;
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
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelTokenResponse;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TunnelAppServiceTest {
    @Mock
    private TunnelRepository tunnelRepository;
    @Mock
    private ClusterRepository clusterRepository;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private TunnelPortRepository tunnelPortRepository;

    @Test
    void createTunnelAllocatesCodeAndReturnsMetadata() {
        RelayProperties properties = new RelayProperties();
        TunnelAppService service = new TunnelAppService(
                tunnelRepository,
                new LocalClusterService(clusterRepository, properties),
                new NamespaceService(),
                new FixedTunnelCodeGenerator(),
                jwtTokenService,
                new TunnelDomainService(),
                tunnelPortRepository,
                properties);
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setClusterId("cluster-a");
        long before = TimeUtils.nowSeconds();

        when(clusterRepository.findByClusterIdAndRegion("cluster-a", "region-a"))
                .thenReturn(Cluster.builder().clusterId("cluster-a").region("region-a").build());
        when(tunnelRepository.existsByTunnelCode(123456L)).thenReturn(false);
        when(tunnelRepository.existsByTunnelId("aaaadysa")).thenReturn(false);
        when(tunnelRepository.save(org.mockito.ArgumentMatchers.any(Tunnel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CreateTunnelResponse response = service.createTunnel("ns-user-001", request);
        long after = TimeUtils.nowSeconds();

        assertThat(response.getTunnelId()).isEqualTo("aaaadysa");
        assertThat(response.getTunnelCode()).isEqualTo(123456L);
        assertThat(response.getUrl()).isEqualTo("aaaadysa-cluster-a-myhuaweicloud.com");
        assertThat(response.getType()).isEqualTo("bridge");
        assertThat(response.getTunnelExpiration())
                .isBetween(Math.toIntExact(before + 72 * 3600L), Math.toIntExact(after + 72 * 3600L));
    }

    @Test
    void createTunnelRejectsClusterOutsideLocalRegion() {
        TunnelAppService service = newService(new RelayProperties());
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setClusterId("cluster-b");

        assertThatThrownBy(() -> service.createTunnel("ns-user-001", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CLUSTER_NOT_FOUND);
    }

    @Test
    void createTunnelUsesCustomExpirationHours() {
        TunnelAppService service = newService(new RelayProperties());
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setClusterId("cluster-a");
        request.setExpiration(2);
        long before = TimeUtils.nowSeconds();

        when(clusterRepository.findByClusterIdAndRegion("cluster-a", "region-a"))
                .thenReturn(Cluster.builder().clusterId("cluster-a").region("region-a").build());
        when(tunnelRepository.existsByTunnelCode(123456L)).thenReturn(false);
        when(tunnelRepository.existsByTunnelId("aaaadysa")).thenReturn(false);
        when(tunnelRepository.save(org.mockito.ArgumentMatchers.any(Tunnel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CreateTunnelResponse response = service.createTunnel("ns-user-001", request);
        long after = TimeUtils.nowSeconds();

        assertThat(response.getTunnelExpiration())
                .isBetween(Math.toIntExact(before + 2 * 3600L), Math.toIntExact(after + 2 * 3600L));
    }

    @Test
    void createTunnelRejectsNonPositiveExpirationHours() {
        TunnelAppService service = newService(new RelayProperties());
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setClusterId("cluster-a");
        request.setExpiration(0);

        when(clusterRepository.findByClusterIdAndRegion("cluster-a", "region-a"))
                .thenReturn(Cluster.builder().clusterId("cluster-a").region("region-a").build());

        assertThatThrownBy(() -> service.createTunnel("ns-user-001", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARAM_INVALID);
    }

    @Test
    void createTunnelRejectsExpirationLongerThan30Days() {
        TunnelAppService service = newService(new RelayProperties());
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setClusterId("cluster-a");
        request.setExpiration(721);

        when(clusterRepository.findByClusterIdAndRegion("cluster-a", "region-a"))
                .thenReturn(Cluster.builder().clusterId("cluster-a").region("region-a").build());

        assertThatThrownBy(() -> service.createTunnel("ns-user-001", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARAM_INVALID);
    }

    @Test
    void createTunnelRejectsWhenNamespaceQuotaExceeded() {
        TunnelAppService service = newService(new RelayProperties());
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setClusterId("cluster-a");

        when(clusterRepository.findByClusterIdAndRegion("cluster-a", "region-a"))
                .thenReturn(Cluster.builder().clusterId("cluster-a").region("region-a").build());
        when(tunnelRepository.countActiveByNamespaceAndRegion(eq("ns-user-001"), eq("region-a"), anyLong()))
                .thenReturn(10L);

        assertThatThrownBy(() -> service.createTunnel("ns-user-001", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_QUOTA_EXCEEDED);
    }

    @Test
    void createTunnelDoesNotExceedQuotaWhenConcurrent() throws Exception {
        AtomicLong activeCount = new AtomicLong();
        RelayProperties properties = new RelayProperties();
        TunnelAppService service = new TunnelAppService(
                tunnelRepository,
                new LocalClusterService(clusterRepository, properties),
                new NamespaceService(),
                new SequenceTunnelCodeGenerator(),
                jwtTokenService,
                new TunnelDomainService(),
                tunnelPortRepository,
                properties);
        CreateTunnelRequest request = new CreateTunnelRequest();
        request.setName("dev");
        request.setClusterId("cluster-a");

        when(clusterRepository.findByClusterIdAndRegion("cluster-a", "region-a"))
                .thenReturn(Cluster.builder().clusterId("cluster-a").region("region-a").build());
        when(tunnelRepository.countActiveByNamespaceAndRegion(eq("ns-user-001"), eq("region-a"), anyLong()))
                .thenAnswer(ignored -> {
                    long count = activeCount.get();
                    Thread.sleep(5);
                    return count;
                });
        when(tunnelRepository.existsByTunnelCode(anyLong())).thenReturn(false);
        when(tunnelRepository.existsByTunnelId(anyString())).thenReturn(false);
        when(tunnelRepository.save(org.mockito.ArgumentMatchers.any(Tunnel.class))).thenAnswer(invocation -> {
            activeCount.incrementAndGet();
            return invocation.getArgument(0);
        });
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Boolean>> tasks = IntStream.range(0, 20)
                    .mapToObj(ignored -> (Callable<Boolean>) () -> createTunnelIfAllowed(service, request))
                    .toList();
            List<Future<Boolean>> futures = executor.invokeAll(tasks);
            long created = futures.stream()
                    .filter(TunnelAppServiceTest::created)
                    .count();

            assertThat(created).isEqualTo(10);
            assertThat(activeCount.get()).isEqualTo(10);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void updateTunnelChangesExpiration() {
        TunnelAppService service = newService(new RelayProperties());
        UpdateTunnelRequest request = new UpdateTunnelRequest();
        request.setExpiration(1);
        Tunnel tunnel = Tunnel.builder()
                .tunnelId("aaaadysa")
                .namespace("ns-user-001")
                .clusterId("cluster-a")
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
    }

    @Test
    void listTunnelsQueriesLocalRegionOnly() {
        TunnelAppService service = newService(new RelayProperties());
        Tunnel local = Tunnel.builder()
                .name("local")
                .namespace("ns-user-001")
                .clusterId("cluster-a")
                .url("local-cluster-a-myhuaweicloud.com")
                .portCount(2L)
                .deleted(0)
                .build();

        when(tunnelRepository.findActiveByNamespaceAndRegion(
                eq("ns-user-001"), isNull(), eq("region-a"), anyLong()))
                .thenReturn(List.of(local));

        List<TunnelListItemResponse> response = service.listTunnels("ns-user-001", null);

        assertThat(response).extracting(TunnelListItemResponse::getName).containsExactly("local");
        assertThat(response.get(0).getPortCount()).isEqualTo(2L);
    }

    @Test
    void listTunnelsQueriesActiveTunnelsOnly() {
        TunnelAppService service = newService(new RelayProperties());

        when(tunnelRepository.findActiveByNamespaceAndRegion(
                eq("ns-user-001"), isNull(), eq("region-a"), anyLong()))
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
                .clusterId("cluster-a")
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
                .clusterId("cluster-a")
                .deleted(0)
                .build();

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel);

        Boolean deleted = service.deleteTunnel("ns-user-001", "aaaadysa");

        assertThat(deleted).isTrue();
        verify(tunnelPortRepository).deleteByTunnelCode(123456L);
        verify(tunnelRepository).deleteByTunnelId("aaaadysa");
    }

    @Test
    void deleteTunnelsCleansLocalRegionTunnelsOnly() {
        TunnelAppService service = newService(new RelayProperties());
        Tunnel first = Tunnel.builder()
                .tunnelId("aaaadysa")
                .tunnelCode(123456L)
                .namespace("ns-user-001")
                .clusterId("cluster-a")
                .deleted(0)
                .build();

        when(tunnelRepository.findByNamespaceAndRegion("ns-user-001", "region-a")).thenReturn(List.of(first));

        Boolean deleted = service.deleteTunnels("ns-user-001");

        assertThat(deleted).isTrue();
        verify(tunnelPortRepository).deleteByTunnelCode(123456L);
        verify(tunnelRepository).deleteByTunnelId("aaaadysa");
    }

    @Test
    void issueTokenReturnsRequestedScopeAndLifetime() {
        TunnelAppService service = newService(new RelayProperties());
        Tunnel tunnel = Tunnel.builder()
                .tunnelId("aaaadysa")
                .namespace("ns-user-001")
                .expiration(Math.toIntExact(TimeUtils.nowSeconds() + 3600))
                .build();
        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel);
        when(jwtTokenService.issueToken(tunnel, JwtScope.HOST))
                .thenReturn(new JwtToken("host-token", 3600L, 200000L));

        TunnelTokenResponse response = service.issueToken("ns-user-001", "aaaadysa", "host");

        assertThat(response.getTunnelId()).isEqualTo("aaaadysa");
        assertThat(response.getScope()).isEqualTo(JwtScope.HOST);
        assertThat(response.getLifetime()).isEqualTo(3600L);
        assertThat(response.getExpiration()).isEqualTo(200000L);
        assertThat(response.getToken()).isEqualTo("host-token");
    }

    @Test
    void issueTokenRejectsUnsupportedScope() {
        TunnelAppService service = newService(new RelayProperties());

        assertThatThrownBy(() -> service.issueToken("ns-user-001", "aaaadysa", "admin"))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARAM_INVALID);
    }

    private TunnelAppService newService(RelayProperties properties) {
        return new TunnelAppService(
                tunnelRepository,
                new LocalClusterService(clusterRepository, properties),
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

    private static class SequenceTunnelCodeGenerator extends TunnelCodeGenerator {
        private final AtomicLong next = new AtomicLong(100000L);

        @Override
        public long generate() {
            return next.incrementAndGet();
        }

        @Override
        public String toTunnelId(long tunnelCode) {
            return "t" + tunnelCode;
        }
    }

    private static boolean createTunnelIfAllowed(TunnelAppService service, CreateTunnelRequest request) {
        try {
            service.createTunnel("ns-user-001", request);
            return true;
        } catch (BizException exception) {
            if (exception.getErrorCode() == ErrorCode.TUNNEL_QUOTA_EXCEEDED) {
                return false;
            }
            throw exception;
        }
    }

    private static boolean created(Future<Boolean> future) {
        try {
            return future.get();
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
