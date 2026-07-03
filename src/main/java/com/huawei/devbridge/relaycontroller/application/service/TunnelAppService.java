package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.StringUtils;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.domain.model.Grid;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
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

    @Transactional
    public CreateTunnelResponse createTunnel(String userId, CreateTunnelRequest request) {
        String namespace = namespaceService.resolveNamespace(userId);
        Grid grid = gridRepository.findByGridName(request.getGridName());
        if (grid == null) {
            throw new BizException(ErrorCode.GRID_NOT_FOUND);
        }
        long now = TimeUtils.nowSeconds();
        int expiration = request.getExpiration() == null
                ? Math.toIntExact(now + relayProperties.getDefaultExpirationSeconds())
                : request.getExpiration();
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
                .type(StringUtils.defaultIfBlank(request.getType(), "default"))
                .deleted(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        tunnelRepository.save(tunnel);
        return CreateTunnelResponse.builder()
                .name(tunnel.getName())
                .id(tunnel.getTunnelId())
                .tunnelId(tunnel.getTunnelId())
                .tunnelCode(tunnel.getTunnelCode())
                .gridName(tunnel.getGridName())
                .cluster(tunnel.getCluster())
                .description(tunnel.getDescription())
                .bandwidthUsed(tunnel.getBandwidthUsed())
                .expiration(tunnel.getExpiration())
                .created(tunnel.getCreatedAt())
                .url(tunnel.getUrl())
                .type(tunnel.getType())
                .build();
    }

    public List<TunnelListItemResponse> listTunnels(String userId, String gridName) {
        String namespace = namespaceService.resolveNamespace(userId);
        return tunnelRepository.findByNamespace(namespace, gridName).stream()
                .map(tunnel -> TunnelListItemResponse.builder()
                        .name(tunnel.getName())
                        .description(tunnel.getDescription())
                        .created(tunnel.getCreatedAt())
                        .url(tunnel.getUrl())
                        .build())
                .toList();
    }

    public TunnelDetailResponse getTunnelDetail(String userId, String tunnelId) {
        String namespace = namespaceService.resolveNamespace(userId);
        Tunnel tunnel = tunnelRepository.findByTunnelId(tunnelId);
        tunnelDomainService.assertOwnedBy(tunnel, namespace);
        tunnelDomainService.assertNotExpired(tunnel);
        return TunnelDetailResponse.builder()
                .name(tunnel.getName())
                .id(tunnel.getTunnelId())
                .tunnelId(tunnel.getTunnelId())
                .tunnelCode(tunnel.getTunnelCode())
                .gridName(tunnel.getGridName())
                .cluster(tunnel.getCluster())
                .description(tunnel.getDescription())
                .bandwidthUsed(tunnel.getBandwidthUsed())
                .expiration(tunnel.getExpiration())
                .created(tunnel.getCreatedAt())
                .url(tunnel.getUrl())
                .type(tunnel.getType())
                .build();
    }

    @Transactional
    public Boolean updateTunnel(String userId, UpdateTunnelRequest request) {
        String namespace = namespaceService.resolveNamespace(userId);
        Tunnel tunnel = tunnelRepository.findByTunnelId(request.getTunnelId());
        tunnelDomainService.assertOwnedBy(tunnel, namespace);
        if (!StringUtils.isBlank(request.getName())) {
            tunnel.setName(request.getName());
        }
        if (request.getDescription() != null) {
            tunnel.setDescription(request.getDescription());
        }
        if (request.getCluster() != null) {
            tunnel.setCluster(request.getCluster());
        }
        boolean expirationChanged = false;
        if (request.getExpiration() != null) {
            assertFutureExpiration(request.getExpiration(), TimeUtils.nowSeconds());
            tunnel.setExpiration(request.getExpiration());
            expirationChanged = true;
        }
        if (!StringUtils.isBlank(request.getType())) {
            tunnel.setType(request.getType());
        }
        tunnel.setUpdatedAt(TimeUtils.nowSeconds());
        tunnelRepository.update(tunnel);
        if (expirationChanged) {
            jwtTokenService.evictReusableToken(tunnel.getTunnelId());
        }
        return true;
    }

    @Transactional
    public Boolean deleteTunnel(String userId, String tunnelId) {
        String namespace = namespaceService.resolveNamespace(userId);
        Tunnel tunnel = tunnelRepository.findByTunnelId(tunnelId);
        tunnelDomainService.assertOwnedBy(tunnel, namespace);
        tunnelRepository.softDelete(tunnelId, TimeUtils.nowSeconds());
        jwtTokenService.evictReusableToken(tunnelId);
        tunnelPortRepository.deleteByTunnelCode(tunnel.getTunnelCode());
        return true;
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
