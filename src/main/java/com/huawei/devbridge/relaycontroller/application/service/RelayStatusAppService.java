package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.domain.model.RelayStatus;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.repository.RelayStatusRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.domain.service.NamespaceService;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelDomainService;
import com.huawei.devbridge.relaycontroller.interfaces.response.RelayStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RelayStatusAppService {
    private final TunnelRepository tunnelRepository;
    private final RelayStatusRepository relayStatusRepository;
    private final NamespaceService namespaceService;
    private final TunnelDomainService tunnelDomainService;

    public RelayStatusResponse getStatus(String userId, String tunnelId) {
        String namespace = namespaceService.resolveNamespace(userId);
        Tunnel tunnel = tunnelRepository.findByTunnelId(tunnelId);
        tunnelDomainService.assertOwnedBy(tunnel, namespace);
        RelayStatus relayStatus = relayStatusRepository.findByTunnelId(tunnelId);
        if (relayStatus == null || !tunnel.getGridName().equals(relayStatus.getGridName())) {
            return offlineStatus(tunnelId, tunnel);
        }
        return RelayStatusResponse.builder()
                .tunnelId(relayStatus.getTunnelId())
                .status(relayStatus.getStatus())
                .gridName(relayStatus.getGridName())
                .nodeId(relayStatus.getNodeId())
                .gatewayIp(relayStatus.getGatewayIp())
                .lastHeartbeat(relayStatus.getLastHeartbeat())
                .build();
    }

    private RelayStatusResponse offlineStatus(String tunnelId, Tunnel tunnel) {
        return RelayStatusResponse.builder()
                .tunnelId(tunnelId)
                .status("OFFLINE")
                .gridName(tunnel.getGridName())
                .build();
    }
}
