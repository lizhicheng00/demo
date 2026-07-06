package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.StringUtils;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.domain.service.JwtTokenService;
import com.huawei.devbridge.relaycontroller.domain.service.NamespaceService;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelDomainService;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenAppService {
    private final TunnelRepository tunnelRepository;
    private final JwtTokenService jwtTokenService;
    private final LocalGridService localGridService;
    private final NamespaceService namespaceService;
    private final TunnelDomainService tunnelDomainService;
    private final RelayProperties relayProperties;

    public CreateTokenResponse createToken(String namespace, CreateTokenRequest request) {
        String tunnelId = request == null ? null : request.getTunnelId();
        if (StringUtils.isBlank(tunnelId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "tunnelId is required");
        }
        Tunnel tunnel = findActiveTunnel(tunnelId);
        if (!StringUtils.isBlank(namespace)) {
            tunnelDomainService.assertOwnedBy(tunnel, namespaceService.requireNamespace(namespace));
        }
        String token = jwtTokenService.getOrCreateToken(tunnel);
        log.info("Token issued: tunnelId={}, gridName={}, mode=direct, namespacePresent={}, expiresIn={}",
                tunnel.getTunnelId(), tunnel.getGridName(), !StringUtils.isBlank(namespace),
                relayProperties.getJwt().getToken().getTtlSeconds());
        return tokenResponse(token);
    }

    private CreateTokenResponse tokenResponse(String token) {
        return CreateTokenResponse.builder()
                .tokenType("TOKEN")
                .token(token)
                .expiresIn(relayProperties.getJwt().getToken().getTtlSeconds())
                .build();
    }

    private Tunnel findActiveTunnel(String tunnelId) {
        Tunnel tunnel = tunnelRepository.findByTunnelId(tunnelId);
        tunnelDomainService.assertNotExpired(tunnel);
        localGridService.requireLocalGrid(tunnel.getGridName());
        return tunnel;
    }
}
