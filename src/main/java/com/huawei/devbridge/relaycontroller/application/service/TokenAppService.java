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
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateRtTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateRtTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenAppService {
    private final TunnelRepository tunnelRepository;
    private final JwtTokenService jwtTokenService;
    private final NamespaceService namespaceService;
    private final TunnelDomainService tunnelDomainService;
    private final RelayProperties relayProperties;

    public CreateRtTokenResponse createRt(String userId, CreateRtTokenRequest request) {
        String tunnelId = request == null ? null : request.getTunnelId();
        if (StringUtils.isBlank(tunnelId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "tunnelId is required");
        }
        Tunnel tunnel = findActiveTunnel(tunnelId);
        if (!StringUtils.isBlank(userId)) {
            tunnelDomainService.assertOwnedBy(tunnel, namespaceService.resolveNamespace(userId));
        }
        String token = jwtTokenService.getOrCreateReusableToken(tunnel);
        log.info("RT issued: tunnelId={}, gridName={}, mode=direct, userIdPresent={}, expiresIn={}",
                tunnel.getTunnelId(), tunnel.getGridName(), !StringUtils.isBlank(userId),
                relayProperties.getJwt().getRt().getTtlSeconds());
        return reusableTokenResponse(token);
    }

    private CreateRtTokenResponse reusableTokenResponse(String token) {
        return CreateRtTokenResponse.builder()
                .tokenType("RT")
                .token(token)
                .expiresIn(relayProperties.getJwt().getRt().getTtlSeconds())
                .build();
    }

    private Tunnel findActiveTunnel(String tunnelId) {
        Tunnel tunnel = tunnelRepository.findByTunnelId(tunnelId);
        tunnelDomainService.assertNotExpired(tunnel);
        return tunnel;
    }
}
