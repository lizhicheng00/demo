package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.application.service.TunnelPortAppService;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.model.TunnelPort;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelPortRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.domain.service.NamespaceService;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelDomainService;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelPortDomainService;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTunnelPortRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.UpdateTunnelPortRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.GatewayTunnelPortPolicyResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelPortResponse;
import java.util.List;
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

    @Test
    void createTunnelPortWritesPolicy() {
        TunnelPortAppService service = newService();
        CreateTunnelPortRequest request = new CreateTunnelPortRequest();
        request.setPort(8080L);
        request.setAllowAnonymous(false);

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel("ns-user-001", "grid-a"));
        when(tunnelPortRepository.existsByTunnelCodeAndPort(123456L, 8080L)).thenReturn(false);
        when(tunnelPortRepository.save(org.mockito.ArgumentMatchers.any(TunnelPort.class))).thenAnswer(invocation -> {
            TunnelPort tunnelPort = invocation.getArgument(0);
            tunnelPort.setId(1L);
            return tunnelPort;
        });

        TunnelPortResponse response = service.create("ns-user-001", "000001e240", request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTunnelId()).isEqualTo("000001e240");
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

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel("ns-user-001", "grid-a"));
        when(tunnelPortRepository.existsByTunnelCodeAndPort(123456L, 8080L)).thenReturn(true);

        assertThatThrownBy(() -> service.create("ns-user-001", "000001e240", request))
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

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel("ns-user-001", "grid-a"));

        assertThatThrownBy(() -> service.create("ns-user-001", "000001e240", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_PORT_INVALID);
    }

    @Test
    void createTunnelPortRejectsNullPort() {
        TunnelPortAppService service = newService();
        CreateTunnelPortRequest request = new CreateTunnelPortRequest();
        request.setAllowAnonymous(false);

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel("ns-user-001", "grid-a"));

        assertThatThrownBy(() -> service.create("ns-user-001", "000001e240", request))
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

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel("ns-user-a", "grid-a"));

        assertThatThrownBy(() -> service.create("ns-user-b", "000001e240", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_ACCESS_DENIED);
    }

    @Test
    void listTunnelPortsReturnsPolicies() {
        TunnelPortAppService service = newService();

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel("ns-user-001", "grid-a"));
        when(tunnelPortRepository.findByTunnelCode(123456L)).thenReturn(List.of(
                TunnelPort.builder().id(1L).tunnelCode(123456L).port(8080L).allowAnonymous(false).build(),
                TunnelPort.builder().id(2L).tunnelCode(123456L).port(8888L).allowAnonymous(true).build()));

        List<TunnelPortResponse> response = service.list("ns-user-001", "000001e240");

        assertThat(response).extracting(TunnelPortResponse::getPort).containsExactly(8080L, 8888L);
    }

    @Test
    void updateTunnelPortOnlyChangesAllowAnonymous() {
        TunnelPortAppService service = newService();
        UpdateTunnelPortRequest request = new UpdateTunnelPortRequest();
        request.setAllowAnonymous(true);

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel("ns-user-001", "grid-a"));
        when(tunnelPortRepository.findByTunnelCodeAndPort(123456L, 8080L))
                .thenReturn(TunnelPort.builder().id(1L).tunnelCode(123456L).port(8080L).allowAnonymous(false).build());

        TunnelPortResponse response = service.update("ns-user-001", "000001e240", 8080L, request);

        assertThat(response.getAllowAnonymous()).isTrue();
        verify(tunnelPortRepository).updateAllowAnonymous(123456L, 8080L, true);
    }

    @Test
    void deleteTunnelPortRemovesPolicy() {
        TunnelPortAppService service = newService();

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel("ns-user-001", "grid-a"));
        when(tunnelPortRepository.findByTunnelCodeAndPort(123456L, 8080L))
                .thenReturn(TunnelPort.builder().id(1L).tunnelCode(123456L).port(8080L).allowAnonymous(false).build());

        Boolean deleted = service.delete("ns-user-001", "000001e240", 8080L);

        assertThat(deleted).isTrue();
        verify(tunnelPortRepository).deleteByTunnelCodeAndPort(123456L, 8080L);
    }

    @Test
    void deleteAllTunnelPortsRemovesPolicies() {
        TunnelPortAppService service = newService();

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel("ns-user-001", "grid-a"));

        Boolean deleted = service.deleteAll("ns-user-001", "000001e240");

        assertThat(deleted).isTrue();
        verify(tunnelPortRepository).deleteByTunnelCode(123456L);
    }

    @Test
    void detailRejectsMissingPort() {
        TunnelPortAppService service = newService();

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel("ns-user-001", "grid-a"));
        when(tunnelPortRepository.findByTunnelCodeAndPort(123456L, 8080L)).thenReturn(null);

        assertThatThrownBy(() -> service.detail("ns-user-001", "000001e240", 8080L))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_PORT_NOT_FOUND);
    }

    @Test
    void gatewayPolicyChecksGridAndReturnsPolicy() {
        TunnelPortAppService service = newService();

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel("ns-user-001", "grid-a"));
        when(tunnelPortRepository.findByTunnelCodeAndPort(123456L, 8080L))
                .thenReturn(TunnelPort.builder().id(1L).tunnelCode(123456L).port(8080L).allowAnonymous(true).build());

        GatewayTunnelPortPolicyResponse response = service.getGatewayPortPolicy("grid-a", "000001e240", 8080L);

        assertThat(response.getGridName()).isEqualTo("grid-a");
        assertThat(response.getAllowAnonymous()).isTrue();
    }

    @Test
    void gatewayPolicyRejectsGridMismatch() {
        TunnelPortAppService service = newService();

        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(tunnel("ns-user-001", "grid-a"));

        assertThatThrownBy(() -> service.getGatewayPortPolicy("grid-b", "000001e240", 8080L))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TUNNEL_PORT_ACCESS_DENIED);
    }

    private TunnelPortAppService newService() {
        return new TunnelPortAppService(
                tunnelRepository,
                tunnelPortRepository,
                new NamespaceService(),
                new TunnelDomainService(),
                new TunnelPortDomainService());
    }

    private Tunnel tunnel(String namespace, String gridName) {
        return Tunnel.builder()
                .tunnelId("000001e240")
                .tunnelCode(123456L)
                .namespace(namespace)
                .gridName(gridName)
                .deleted(0)
                .build();
    }
}
