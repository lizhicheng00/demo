package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.application.assembler.TunnelAssembler;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.domain.model.Grid;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.model.TunnelType;
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
    private static final int SECONDS_PER_HOUR = 3600;
    private final TunnelRepository tunnelRepository;
    private final LocalGridService localGridService;
    private final NamespaceService namespaceService;
    private final TunnelCodeGenerator tunnelCodeGenerator;
    private final JwtTokenService jwtTokenService;
    private final TunnelDomainService tunnelDomainService;
    private final TunnelPortRepository tunnelPortRepository;
    private final RelayProperties relayProperties;

    public CreateTunnelResponse createTunnel(String rawNamespace, CreateTunnelRequest request) {
        String namespace = namespaceService.requireNamespace(rawNamespace);
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

    public List<TunnelListItemResponse> listTunnels(String rawNamespace, String gridName) {
        String namespace = namespaceService.requireNamespace(rawNamespace);
        if (gridName != null && !gridName.isBlank()) {
            localGridService.requireLocalGrid(gridName);
            return tunnelRepository.findByNamespace(namespace, gridName).stream()
                    .map(TunnelAssembler::toListItem)
                    .toList();
        }
        return tunnelRepository.findByNamespace(namespace, gridName).stream()
                .filter(tunnel -> localGridService.isLocalGrid(tunnel.getGridName()))
                .map(TunnelAssembler::toListItem)
                .toList();
    }

    public TunnelDetailResponse getTunnelDetail(String rawNamespace, String tunnelId) {
        Tunnel tunnel = findOwnedTunnel(rawNamespace, tunnelId);
        tunnelDomainService.assertNotExpired(tunnel);
        return TunnelAssembler.toDetailResponse(tunnel);
    }

    @Transactional
    public Boolean updateTunnel(String rawNamespace, UpdateTunnelRequest request) {
        Tunnel tunnel = findOwnedTunnel(rawNamespace, request.getTunnelId());
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
    public Boolean deleteTunnel(String rawNamespace, String tunnelId) {
        Tunnel tunnel = findOwnedTunnel(rawNamespace, tunnelId);
        tunnelRepository.softDelete(tunnelId, TimeUtils.nowSeconds());
        jwtTokenService.evictToken(tunnelId);
        tunnelPortRepository.deleteByTunnelCode(tunnel.getTunnelCode());
        log.info("Tunnel deleted: tunnelId={}, tunnelCode={}, namespace={}",
                tunnel.getTunnelId(), tunnel.getTunnelCode(), tunnel.getNamespace());
        return true;
    }

    @Transactional
    public Boolean deleteTunnels(String rawNamespace) {
        String namespace = namespaceService.requireNamespace(rawNamespace);
        List<Tunnel> tunnels = tunnelRepository.findByNamespace(namespace, null).stream()
                .filter(tunnel -> localGridService.isLocalGrid(tunnel.getGridName()))
                .toList();
        long now = TimeUtils.nowSeconds();
        tunnels.forEach(tunnel -> {
            tunnelRepository.softDelete(tunnel.getTunnelId(), now);
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
            tunnel.setExpiration(resolveExpiration(request.getExpiration(), TimeUtils.nowSeconds()));
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

    private Tunnel findOwnedTunnel(String rawNamespace, String tunnelId) {
        String namespace = namespaceService.requireNamespace(rawNamespace);
        Tunnel tunnel = tunnelRepository.findByTunnelId(tunnelId);
        tunnelDomainService.assertOwnedBy(tunnel, namespace);
        localGridService.requireLocalGrid(tunnel.getGridName());
        return tunnel;
    }

    private Grid findGrid(String gridName) {
        return localGridService.requireLocalGrid(gridName);
    }

    private int resolveExpiration(Integer expirationHours, long now) {
        int hours = expirationHours == null ? relayProperties.getDefaultExpirationHours() : expirationHours;
        if (hours <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "expiration must be positive hours");
        }
        long expiresAt = now + (long) hours * SECONDS_PER_HOUR;
        if (expiresAt > Integer.MAX_VALUE) {
            throw new BizException(ErrorCode.PARAM_INVALID, "expiration is too large");
        }
        return Math.toIntExact(expiresAt);
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
