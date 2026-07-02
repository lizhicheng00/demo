package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
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
        if (relayStatus == null) {
            return RelayStatusResponse.builder()
                    .tunnelId(tunnelId)
                    .status("OFFLINE")
                    .gridname(tunnel.getGridname())
                    .lastHeartbeat(TimeUtils.nowSeconds())
                    .build();
        }
        return RelayStatusResponse.builder()
                .tunnelId(relayStatus.getTunnelId())
                .status(relayStatus.getStatus())
                .gridname(relayStatus.getGridname())
                .nodeId(relayStatus.getNodeId())
                .gatewayIp(relayStatus.getGatewayIp())
                .lastHeartbeat(relayStatus.getLastHeartbeat())
                .build();
    }
}
