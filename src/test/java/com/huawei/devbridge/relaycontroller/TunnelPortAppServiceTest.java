package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.application.service.LocalClusterService;
import com.huawei.devbridge.relaycontroller.application.service.TunnelPortAppService;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.domain.model.Cluster;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.model.TunnelPort;
import com.huawei.devbridge.relaycontroller.domain.repository.ClusterRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelPortRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.domain.service.NamespaceService;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelDomainService;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelPortDomainService;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTunnelPortRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.UpdateTunnelPortRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.GatewayTunnelPortPolicyResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelPortResponse;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TunnelPortAppServiceTest {
    @Mock
    private TunnelRepository tunnelRepository;
    @Mock
    private TunnelPortRepository tunnelPortRepository;
    @Mock
    private ClusterRepository clusterRepository;

    @BeforeEach
    void setUp() {
        localCluster("cluster-a");
    }

    @Test
    void createTunnelPortWritesPolicy() {
        TunnelPortAppService service = newService();
        CreateTunnelPortRequest request = new CreateTunnelPortRequest();
        request.setPort(8080L);
        request.setAllowAnonymous(false);

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel("ns-user-001", "cluster-a"));
        when(tunnelPortRepository.existsByTunnelCodeAndPort(123456L, 8080L)).thenReturn(false);
        when(tunnelPortRepository.save(org.mockito.ArgumentMatchers.any(TunnelPort.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TunnelPortResponse response = service.create("ns-user-001", "aaaadysa", request);

        assertThat(response.getTunnelId()).isEqualTo("aaaadysa");
        assertThat(response.getTunnelCode()).isEqualTo(123456L);
        assertThat(response.getPort()).isEqualTo(8080L);
        assertThat(response.getAllowAnonymous()).isFalse();
    }

    @Test
    void createTunnelPortRejectsDuplicatePort() {
        TunnelPortAppService service = newService();
        CreateTunnelPortRequest request = new CreateTunnelPortRequest();
        request.setPort(8080L);
        request.setAllowAnonymous(false);

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel("ns-user-001", "cluster-a"));
        when(tunnelPortRepository.existsByTunnelCodeAndPort(123456L, 8080L)).thenReturn(true);

        assertThatThrownBy(() -> service.create("ns-user-001", "aaaadysa", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_PORT_ALREADY_EXISTS);
    }

    @Test
    void createTunnelPortRejectsInvalidPort() {
        TunnelPortAppService service = newService();
        CreateTunnelPortRequest request = new CreateTunnelPortRequest();
        request.setPort(65536L);
        request.setAllowAnonymous(false);

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel("ns-user-001", "cluster-a"));

        assertThatThrownBy(() -> service.create("ns-user-001", "aaaadysa", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_PORT_INVALID);
    }

    @Test
    void createTunnelPortRejectsNullPort() {
        TunnelPortAppService service = newService();
        CreateTunnelPortRequest request = new CreateTunnelPortRequest();
        request.setAllowAnonymous(false);

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel("ns-user-001", "cluster-a"));

        assertThatThrownBy(() -> service.create("ns-user-001", "aaaadysa", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_PORT_INVALID);
    }

    @Test
    void createTunnelPortRejectsNamespaceMismatch() {
        TunnelPortAppService service = newService();
        CreateTunnelPortRequest request = new CreateTunnelPortRequest();
        request.setPort(8080L);
        request.setAllowAnonymous(false);

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel("ns-user-a", "cluster-a"));

        assertThatThrownBy(() -> service.create("ns-user-b", "aaaadysa", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_ACCESS_DENIED);
    }

    @Test
    void listTunnelPortsReturnsPolicies() {
        TunnelPortAppService service = newService();

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel("ns-user-001", "cluster-a"));
        when(tunnelPortRepository.findByTunnelCode(123456L)).thenReturn(List.of(
                TunnelPort.builder().tunnelCode(123456L).port(8080L).allowAnonymous(false).build(),
                TunnelPort.builder().tunnelCode(123456L).port(8888L).allowAnonymous(true).build()));

        List<TunnelPortResponse> response = service.list("ns-user-001", "aaaadysa");

        assertThat(response).extracting(TunnelPortResponse::getPort).containsExactly(8080L, 8888L);
    }

    @Test
    void updateTunnelPortOnlyChangesAllowAnonymous() {
        TunnelPortAppService service = newService();
        UpdateTunnelPortRequest request = new UpdateTunnelPortRequest();
        request.setAllowAnonymous(true);

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel("ns-user-001", "cluster-a"));
        when(tunnelPortRepository.findByTunnelCodeAndPort(123456L, 8080L))
                .thenReturn(TunnelPort.builder().tunnelCode(123456L).port(8080L).allowAnonymous(false).build());

        TunnelPortResponse response = service.update("ns-user-001", "aaaadysa", 8080L, request);

        assertThat(response.getAllowAnonymous()).isTrue();
        verify(tunnelPortRepository).updateAllowAnonymous(123456L, 8080L, true);
    }

    @Test
    void deleteTunnelPortRemovesPolicy() {
        TunnelPortAppService service = newService();

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel("ns-user-001", "cluster-a"));
        when(tunnelPortRepository.findByTunnelCodeAndPort(123456L, 8080L))
                .thenReturn(TunnelPort.builder().tunnelCode(123456L).port(8080L).allowAnonymous(false).build());

        Boolean deleted = service.delete("ns-user-001", "aaaadysa", 8080L);

        assertThat(deleted).isTrue();
        verify(tunnelPortRepository).deleteByTunnelCodeAndPort(123456L, 8080L);
    }

    @Test
    void deleteAllTunnelPortsRemovesPolicies() {
        TunnelPortAppService service = newService();

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel("ns-user-001", "cluster-a"));

        Boolean deleted = service.deleteAll("ns-user-001", "aaaadysa");

        assertThat(deleted).isTrue();
        verify(tunnelPortRepository).deleteByTunnelCode(123456L);
    }

    @Test
    void detailRejectsMissingPort() {
        TunnelPortAppService service = newService();

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel("ns-user-001", "cluster-a"));
        when(tunnelPortRepository.findByTunnelCodeAndPort(123456L, 8080L)).thenReturn(null);

        assertThatThrownBy(() -> service.detail("ns-user-001", "aaaadysa", 8080L))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_PORT_NOT_FOUND);
    }

    @Test
    void gatewayPolicyChecksClusterAndReturnsPolicy() {
        TunnelPortAppService service = newService();

        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel("ns-user-001", "cluster-a"));
        when(tunnelPortRepository.findByTunnelCodeAndPort(123456L, 8080L))
                .thenReturn(TunnelPort.builder().tunnelCode(123456L).port(8080L).allowAnonymous(true).build());

        GatewayTunnelPortPolicyResponse response = service.getGatewayPortPolicy("cluster-a", "aaaadysa", 8080L);

        assertThat(response.getClusterId()).isEqualTo("cluster-a");
        assertThat(response.getAllowAnonymous()).isTrue();
    }

    @Test
    void gatewayPolicyRejectsClusterMismatch() {
        TunnelPortAppService service = newService();

        localCluster("cluster-b");
        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(tunnel("ns-user-001", "cluster-a"));

        assertThatThrownBy(() -> service.getGatewayPortPolicy("cluster-b", "aaaadysa", 8080L))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_PORT_ACCESS_DENIED);
    }

    @Test
    void gatewayPolicyRejectsClusterOutsideLocalRegion() {
        TunnelPortAppService service = newService();

        assertThatThrownBy(() -> service.getGatewayPortPolicy("cluster-b", "aaaadysa", 8080L))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CLUSTER_NOT_FOUND);
    }

    private TunnelPortAppService newService() {
        RelayProperties properties = new RelayProperties();
        return new TunnelPortAppService(
                tunnelRepository,
                tunnelPortRepository,
                new LocalClusterService(clusterRepository, properties),
                new NamespaceService(),
                new TunnelDomainService(),
                new TunnelPortDomainService(),
                properties);
    }

    private Tunnel tunnel(String namespace, String clusterId) {
        return Tunnel.builder()
                .tunnelId("aaaadysa")
                .tunnelCode(123456L)
                .namespace(namespace)
                .clusterId(clusterId)
                .deleted(0)
                .build();
    }

    private void localCluster(String clusterId) {
        lenient().when(clusterRepository.findByClusterIdAndRegion(clusterId, "region-a"))
                .thenReturn(Cluster.builder().clusterId(clusterId).region("region-a").build());
    }
}
