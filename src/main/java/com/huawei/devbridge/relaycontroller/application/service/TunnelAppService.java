package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.application.assembler.TunnelAssembler;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
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
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelDetailResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelListItemResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TunnelAppService {
    private static final int TUNNEL_CODE_MAX_RETRY = 5;
    private final TunnelRepository tunnelRepository;
    private final GridRepository gridRepository;
    private final NamespaceService namespaceService;
    private final TunnelCodeGenerator tunnelCodeGenerator;
    private final JwtTokenService jwtTokenService;
    private final TunnelDomainService tunnelDomainService;
    private final TunnelPortRepository tunnelPortRepository;
    private final RelayProperties relayProperties;

    public CreateTunnelResponse createTunnel(String userId, CreateTunnelRequest request) {
        String namespace = namespaceService.resolveNamespace(userId);
        TunnelType type = request.getType() == null ? TunnelType.BRIDGE : request.getType();
        Grid grid = findGrid(request.getGridName());
        long now = TimeUtils.nowSeconds();
        int expiration = resolveExpiration(request.getExpiration(), now);
        assertFutureExpiration(expiration, now);
        TunnelCode code = allocateTunnelCode();
        Tunnel tunnel = Tunnel.builder()
                .name(request.getName())
                .tunnelId(code.tunnelId())
                .tunnelCode(code.tunnelCode())
                .gridName(request.getGridName())
                .expiration(expiration)
                .namespace(namespace)
                .description(request.getDescription())
                .cluster(request.getCluster())
                .bandwidthUsed(0L)
                .url(buildTunnelUrl(code.tunnelId(), grid))
                .type(type)
                .deleted(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        tunnelRepository.save(tunnel);
        log.info("Tunnel created: tunnelId={}, tunnelCode={}, namespace={}, gridName={}, type={}, expiration={}",
                tunnel.getTunnelId(), tunnel.getTunnelCode(), tunnel.getNamespace(), tunnel.getGridName(),
                tunnel.getType(), tunnel.getExpiration());
        return TunnelAssembler.toCreateResponse(tunnel);
    }

    public List<TunnelListItemResponse> listTunnels(String userId, String gridName) {
        String namespace = namespaceService.resolveNamespace(userId);
        return tunnelRepository.findByNamespace(namespace, gridName).stream()
                .map(TunnelAssembler::toListItem)
                .toList();
    }

    public TunnelDetailResponse getTunnelDetail(String userId, String tunnelId) {
        Tunnel tunnel = findOwnedTunnel(userId, tunnelId);
        tunnelDomainService.assertNotExpired(tunnel);
        return TunnelAssembler.toDetailResponse(tunnel);
    }

    @Transactional
    public Boolean updateTunnel(String userId, UpdateTunnelRequest request) {
        Tunnel tunnel = findOwnedTunnel(userId, request.getTunnelId());
        boolean expirationChanged = applyUpdates(tunnel, request);
        tunnel.setUpdatedAt(TimeUtils.nowSeconds());
        tunnelRepository.update(tunnel);
        if (expirationChanged) {
            jwtTokenService.evictToken(tunnel.getTunnelId());
        }
        log.info("Tunnel updated: tunnelId={}, namespace={}, expirationChanged={}",
                tunnel.getTunnelId(), tunnel.getNamespace(), expirationChanged);
        return true;
    }

    @Transactional
    public Boolean deleteTunnel(String userId, String tunnelId) {
        Tunnel tunnel = findOwnedTunnel(userId, tunnelId);
        tunnelRepository.softDelete(tunnelId, TimeUtils.nowSeconds());
        jwtTokenService.evictToken(tunnelId);
        tunnelPortRepository.deleteByTunnelCode(tunnel.getTunnelCode());
        log.info("Tunnel deleted: tunnelId={}, tunnelCode={}, namespace={}",
                tunnel.getTunnelId(), tunnel.getTunnelCode(), tunnel.getNamespace());
        return true;
    }

    @Transactional
    public Boolean deleteTunnels(String userId) {
        String namespace = namespaceService.resolveNamespace(userId);
        List<Tunnel> tunnels = tunnelRepository.findByNamespace(namespace, null);
        long now = TimeUtils.nowSeconds();
        tunnelRepository.softDeleteByNamespace(namespace, now);
        tunnels.forEach(tunnel -> {
            jwtTokenService.evictToken(tunnel.getTunnelId());
            tunnelPortRepository.deleteByTunnelCode(tunnel.getTunnelCode());
        });
        log.info("Tunnels deleted: namespace={}, count={}", namespace, tunnels.size());
        return true;
    }

    private boolean applyUpdates(Tunnel tunnel, UpdateTunnelRequest request) {
        boolean expirationChanged = false;
        if (request.getName() != null && !request.getName().isBlank()) {
            tunnel.setName(request.getName());
        }
        if (request.getDescription() != null) {
            tunnel.setDescription(request.getDescription());
        }
        if (request.getCluster() != null) {
            tunnel.setCluster(request.getCluster());
        }
        if (request.getExpiration() != null) {
            assertFutureExpiration(request.getExpiration(), TimeUtils.nowSeconds());
            tunnel.setExpiration(request.getExpiration());
            expirationChanged = true;
        }
        if (request.getType() != null) {
            tunnel.setType(request.getType());
        }
        return expirationChanged;
    }

    private TunnelCode allocateTunnelCode() {
        for (int i = 0; i < TUNNEL_CODE_MAX_RETRY; i++) {
            long tunnelCode = tunnelCodeGenerator.generate();
            String tunnelId = tunnelCodeGenerator.toTunnelId(tunnelCode);
            if (!tunnelRepository.existsByTunnelCode(tunnelCode) && !tunnelRepository.existsByTunnelId(tunnelId)) {
                return new TunnelCode(tunnelCode, tunnelId);
            }
        }
        throw new BizException(ErrorCode.TUNNEL_ID_CONFLICT);
    }

    private Tunnel findOwnedTunnel(String userId, String tunnelId) {
        String namespace = namespaceService.resolveNamespace(userId);
        Tunnel tunnel = tunnelRepository.findByTunnelId(tunnelId);
        tunnelDomainService.assertOwnedBy(tunnel, namespace);
        return tunnel;
    }

    private Grid findGrid(String gridName) {
        Grid grid = gridRepository.findByGridName(gridName);
        if (grid == null) {
            throw new BizException(ErrorCode.GRID_NOT_FOUND);
        }
        return grid;
    }

    private int resolveExpiration(Integer expiration, long now) {
        return expiration == null
                ? Math.toIntExact(now + relayProperties.getDefaultExpirationSeconds())
                : expiration;
    }

    private String buildTunnelUrl(String tunnelId, Grid grid) {
        return tunnelId + "." + grid.getRegion() + "." + relayProperties.getDomain();
    }

    private void assertFutureExpiration(int expiration, long now) {
        if (expiration <= now) {
            throw new BizException(ErrorCode.TUNNEL_EXPIRED);
        }
    }

    private record TunnelCode(long tunnelCode, String tunnelId) {
    }
}
