package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.application.assembler.TunnelPortAssembler;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TunnelPortAppService {
    private final TunnelRepository tunnelRepository;
    private final TunnelPortRepository tunnelPortRepository;
    private final NamespaceService namespaceService;
    private final TunnelDomainService tunnelDomainService;
    private final TunnelPortDomainService tunnelPortDomainService;

    @Transactional
    public TunnelPortResponse create(String userId, String tunnelId, CreateTunnelPortRequest request) {
        Tunnel tunnel = ownedTunnel(userId, tunnelId);
        tunnelPortDomainService.validatePort(request.getPort());
        tunnelPortDomainService.validateAllowAnonymous(request.getAllowAnonymous());
        if (tunnelPortRepository.existsByTunnelCodeAndPort(tunnel.getTunnelCode(), request.getPort())) {
            throw new BizException(ErrorCode.TUNNEL_PORT_ALREADY_EXISTS);
        }

        TunnelPort tunnelPort = tunnelPortRepository.save(TunnelPort.builder()
                .tunnelCode(tunnel.getTunnelCode())
                .port(request.getPort())
                .allowAnonymous(request.getAllowAnonymous())
                .build());
        return TunnelPortAssembler.toResponse(tunnel, tunnelPort);
    }

    public List<TunnelPortResponse> list(String userId, String tunnelId) {
        Tunnel tunnel = ownedTunnel(userId, tunnelId);
        return tunnelPortRepository.findByTunnelCode(tunnel.getTunnelCode()).stream()
                .map(tunnelPort -> TunnelPortAssembler.toResponse(tunnel, tunnelPort))
                .toList();
    }

    public TunnelPortResponse detail(String userId, String tunnelId, Long port) {
        Tunnel tunnel = ownedTunnel(userId, tunnelId);
        TunnelPort tunnelPort = findTunnelPort(tunnel.getTunnelCode(), port);
        return TunnelPortAssembler.toResponse(tunnel, tunnelPort);
    }

    @Transactional
    public TunnelPortResponse update(String userId, String tunnelId, Long port, UpdateTunnelPortRequest request) {
        Tunnel tunnel = ownedTunnel(userId, tunnelId);
        tunnelPortDomainService.validateAllowAnonymous(request.getAllowAnonymous());
        TunnelPort tunnelPort = findTunnelPort(tunnel.getTunnelCode(), port);
        tunnelPortRepository.updateAllowAnonymous(tunnel.getTunnelCode(), port, request.getAllowAnonymous());
        tunnelPort.setAllowAnonymous(request.getAllowAnonymous());
        return TunnelPortAssembler.toResponse(tunnel, tunnelPort);
    }

    @Transactional
    public Boolean delete(String userId, String tunnelId, Long port) {
        Tunnel tunnel = ownedTunnel(userId, tunnelId);
        findTunnelPort(tunnel.getTunnelCode(), port);
        tunnelPortRepository.deleteByTunnelCodeAndPort(tunnel.getTunnelCode(), port);
        return true;
    }

    public GatewayTunnelPortPolicyResponse getGatewayPortPolicy(String gridName, String tunnelId, Long port) {
        Tunnel tunnel = tunnelRepository.findByTunnelId(tunnelId);
        tunnelDomainService.assertInGrid(tunnel, gridName, ErrorCode.TUNNEL_PORT_ACCESS_DENIED);
        TunnelPort tunnelPort = findTunnelPort(tunnel.getTunnelCode(), port);
        return TunnelPortAssembler.toGatewayPolicy(tunnel, tunnelPort);
    }

    private Tunnel ownedTunnel(String userId, String tunnelId) {
        String namespace = namespaceService.resolveNamespace(userId);
        Tunnel tunnel = tunnelRepository.findByTunnelId(tunnelId);
        tunnelDomainService.assertOwnedBy(tunnel, namespace);
        return tunnel;
    }

    private TunnelPort findTunnelPort(Long tunnelCode, Long port) {
        tunnelPortDomainService.validatePort(port);
        TunnelPort tunnelPort = tunnelPortRepository.findByTunnelCodeAndPort(tunnelCode, port);
        if (tunnelPort == null) {
            throw new BizException(ErrorCode.TUNNEL_PORT_NOT_FOUND);
        }
        return tunnelPort;
    }
}
