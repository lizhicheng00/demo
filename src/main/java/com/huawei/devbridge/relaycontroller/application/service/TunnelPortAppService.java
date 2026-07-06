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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TunnelPortAppService {
    private final TunnelRepository tunnelRepository;
    private final TunnelPortRepository tunnelPortRepository;
    private final NamespaceService namespaceService;
    private final TunnelDomainService tunnelDomainService;
    private final TunnelPortDomainService tunnelPortDomainService;

    @Transactional
    public TunnelPortResponse create(String rawNamespace, String tunnelId, CreateTunnelPortRequest request) {
        Tunnel tunnel = ownedTunnel(rawNamespace, tunnelId);
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
        log.info("Tunnel port created: tunnelId={}, tunnelCode={}, port={}, allowAnonymous={}",
                tunnel.getTunnelId(), tunnel.getTunnelCode(), tunnelPort.getPort(), tunnelPort.getAllowAnonymous());
        return TunnelPortAssembler.toResponse(tunnel, tunnelPort);
    }

    public List<TunnelPortResponse> list(String rawNamespace, String tunnelId) {
        Tunnel tunnel = ownedTunnel(rawNamespace, tunnelId);
        return tunnelPortRepository.findByTunnelCode(tunnel.getTunnelCode()).stream()
                .map(tunnelPort -> TunnelPortAssembler.toResponse(tunnel, tunnelPort))
                .toList();
    }

    public TunnelPortResponse detail(String rawNamespace, String tunnelId, Long port) {
        Tunnel tunnel = ownedTunnel(rawNamespace, tunnelId);
        TunnelPort tunnelPort = findTunnelPort(tunnel.getTunnelCode(), port);
        return TunnelPortAssembler.toResponse(tunnel, tunnelPort);
    }

    @Transactional
    public TunnelPortResponse update(String rawNamespace, String tunnelId, Long port, UpdateTunnelPortRequest request) {
        Tunnel tunnel = ownedTunnel(rawNamespace, tunnelId);
        tunnelPortDomainService.validateAllowAnonymous(request.getAllowAnonymous());
        TunnelPort tunnelPort = findTunnelPort(tunnel.getTunnelCode(), port);
        tunnelPortRepository.updateAllowAnonymous(tunnel.getTunnelCode(), port, request.getAllowAnonymous());
        tunnelPort.setAllowAnonymous(request.getAllowAnonymous());
        log.info("Tunnel port updated: tunnelId={}, tunnelCode={}, port={}, allowAnonymous={}",
                tunnel.getTunnelId(), tunnel.getTunnelCode(), port, request.getAllowAnonymous());
        return TunnelPortAssembler.toResponse(tunnel, tunnelPort);
    }

    @Transactional
    public Boolean delete(String rawNamespace, String tunnelId, Long port) {
        Tunnel tunnel = ownedTunnel(rawNamespace, tunnelId);
        findTunnelPort(tunnel.getTunnelCode(), port);
        tunnelPortRepository.deleteByTunnelCodeAndPort(tunnel.getTunnelCode(), port);
        log.info("Tunnel port deleted: tunnelId={}, tunnelCode={}, port={}",
                tunnel.getTunnelId(), tunnel.getTunnelCode(), port);
        return true;
    }

    @Transactional
    public Boolean deleteAll(String rawNamespace, String tunnelId) {
        Tunnel tunnel = ownedTunnel(rawNamespace, tunnelId);
        tunnelPortRepository.deleteByTunnelCode(tunnel.getTunnelCode());
        log.info("Tunnel ports deleted: tunnelId={}, tunnelCode={}", tunnel.getTunnelId(), tunnel.getTunnelCode());
        return true;
    }

    public GatewayTunnelPortPolicyResponse getGatewayPortPolicy(String gridName, String tunnelId, Long port) {
        Tunnel tunnel = tunnelRepository.findByTunnelId(tunnelId);
        tunnelDomainService.assertInGrid(tunnel, gridName, ErrorCode.TUNNEL_PORT_ACCESS_DENIED);
        TunnelPort tunnelPort = findTunnelPort(tunnel.getTunnelCode(), port);
        return TunnelPortAssembler.toGatewayPolicy(tunnel, tunnelPort);
    }

    private Tunnel ownedTunnel(String rawNamespace, String tunnelId) {
        String namespace = namespaceService.requireNamespace(rawNamespace);
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
