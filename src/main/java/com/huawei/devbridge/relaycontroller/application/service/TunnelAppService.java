package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.application.assembler.TunnelAssembler;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.domain.model.Grid;
import com.huawei.devbridge.relaycontroller.domain.model.JwtToken;
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
import java.util.concurrent.ConcurrentHashMap;
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
    private final ConcurrentHashMap<String, Object> createLocks = new ConcurrentHashMap<>();

    @Transactional
    public CreateTunnelResponse createTunnel(String rawNamespace, CreateTunnelRequest request) {
        String namespace = namespaceService.requireNamespace(rawNamespace);
        Object lock = createLocks.computeIfAbsent(lockKey(namespace), ignored -> new Object());
        synchronized (lock) {
            return createTunnelLocked(namespace, request);
        }
    }

    private CreateTunnelResponse createTunnelLocked(String namespace, CreateTunnelRequest request) {
        TunnelType type = request.getType() == null ? TunnelType.BRIDGE : request.getType();
        Grid grid = localGridService.requireLocalGrid(request.getGridName());
        long now = TimeUtils.nowSeconds();
        assertTunnelQuota(namespace, now);
        int expiration = resolveExpiration(request.getExpiration(), now);
        TunnelCode code = allocateTunnelCode();
        Tunnel tunnel = Tunnel.builder()
                .name(request.getName())
                .tunnelId(code.tunnelId())
                .tunnelCode(code.tunnelCode())
                .gridName(grid.getGrid())
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

    private String lockKey(String namespace) {
        return relayProperties.getRegion() + ":" + namespace;
    }

    public List<TunnelListItemResponse> listTunnels(String rawNamespace, String gridName) {
        String namespace = namespaceService.requireNamespace(rawNamespace);
        long now = TimeUtils.nowSeconds();
        if (gridName != null && !gridName.isBlank()) {
            localGridService.requireLocalGrid(gridName);
            return tunnelRepository.findActiveByNamespaceAndRegion(namespace, gridName, relayProperties.getRegion(), now).stream()
                    .map(TunnelAssembler::toListItem)
                    .toList();
        }
        return tunnelRepository.findActiveByNamespaceAndRegion(namespace, null, relayProperties.getRegion(), now).stream()
                .map(TunnelAssembler::toListItem)
                .toList();
    }

    public TunnelDetailResponse getTunnelDetail(String rawNamespace, String tunnelId) {
        Tunnel tunnel = findOwnedTunnel(rawNamespace, tunnelId);
        tunnelDomainService.assertNotExpired(tunnel);
        JwtToken jwtToken = jwtTokenService.getOrCreateToken(tunnel);
        return TunnelAssembler.toDetailResponse(tunnel, jwtToken.token(), jwtToken.expiresIn());
    }

    @Transactional
    public Boolean updateTunnel(String rawNamespace, String tunnelId, UpdateTunnelRequest request) {
        Tunnel tunnel = findOwnedActiveTunnel(rawNamespace, tunnelId);
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
        List<Tunnel> tunnels = tunnelRepository.findByNamespaceAndRegion(namespace, relayProperties.getRegion());
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

    private void assertTunnelQuota(String namespace, long now) {
        int maxTunnels = relayProperties.getTunnel().getMaxPerNamespace();
        if (maxTunnels <= 0) {
            return;
        }
        long activeCount = tunnelRepository.countActiveByNamespaceAndRegion(namespace, relayProperties.getRegion(), now);
        if (activeCount >= maxTunnels) {
            throw new BizException(ErrorCode.TUNNEL_QUOTA_EXCEEDED,
                    "active tunnel quota exceeded: max=" + maxTunnels);
        }
    }

    private Tunnel findOwnedTunnel(String rawNamespace, String tunnelId) {
        String namespace = namespaceService.requireNamespace(rawNamespace);
        Tunnel tunnel = tunnelRepository.findByTunnelIdAndRegion(tunnelId, relayProperties.getRegion());
        tunnelDomainService.assertOwnedBy(tunnel, namespace);
        return tunnel;
    }

    private Tunnel findOwnedActiveTunnel(String rawNamespace, String tunnelId) {
        String namespace = namespaceService.requireNamespace(rawNamespace);
        Tunnel tunnel = tunnelRepository.findByTunnelIdAndRegion(tunnelId, relayProperties.getRegion());
        tunnelDomainService.assertOwnedAndNotExpired(tunnel, namespace);
        return tunnel;
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
        return tunnelId + "-" + grid.getGrid() + "-" + relayProperties.getDomain();
    }

    private record TunnelCode(long tunnelCode, String tunnelId) {
    }
}
