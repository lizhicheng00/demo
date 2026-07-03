package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.IdUtils;
import com.huawei.devbridge.relaycontroller.common.util.StringUtils;
import com.huawei.devbridge.relaycontroller.domain.model.CreateOttTokenCommand;
import com.huawei.devbridge.relaycontroller.domain.model.OttClaims;
import com.huawei.devbridge.relaycontroller.domain.model.OttConsumeResult;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.domain.service.JwtTokenService;
import com.huawei.devbridge.relaycontroller.domain.service.NamespaceService;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelDomainService;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateOttTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateRtTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateOttTokenResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateRtTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenAppService {
    private static final String BEARER_PREFIX = "Bearer ";
    private final TunnelRepository tunnelRepository;
    private final JwtTokenService jwtTokenService;
    private final NamespaceService namespaceService;
    private final TunnelDomainService tunnelDomainService;
    private final RelayProperties relayProperties;

    public CreateOttTokenResponse createOtt(CreateOttTokenRequest request) {
        Tunnel tunnel = findActiveTunnel(request.getTunnelId());
        if (!StringUtils.isBlank(request.getGridName()) && !request.getGridName().equals(tunnel.getGridName())) {
            throw new BizException(ErrorCode.TUNNEL_ACCESS_DENIED, "token grid mismatch");
        }
        String token = jwtTokenService.createOneTimeToken(CreateOttTokenCommand.builder()
                .jti(buildOttJti(tunnel.getTunnelId(), request.getConnId()))
                .tunnel(tunnel)
                .connId(request.getConnId())
                .callbackUrl(request.getCallbackUrl())
                .requestPort(request.getRequestPort())
                .build());
        return CreateOttTokenResponse.builder()
                .tokenType("OTT")
                .token(token)
                .expiresIn(relayProperties.getJwt().getOtt().getTtlSeconds())
                .build();
    }

    public CreateRtTokenResponse createRt(String relayAuthorization, String userId, CreateRtTokenRequest request) {
        if (!StringUtils.isBlank(relayAuthorization)) {
            return createRtWithOtt(relayAuthorization, request);
        }
        String tunnelId = request == null ? null : request.getTunnelId();
        if (StringUtils.isBlank(tunnelId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "tunnelId is required");
        }
        Tunnel tunnel = findActiveTunnel(tunnelId);
        if (!StringUtils.isBlank(userId)) {
            tunnelDomainService.assertOwnedBy(tunnel, namespaceService.resolveNamespace(userId));
        }
        return reusableTokenResponse(jwtTokenService.getOrCreateReusableToken(tunnel));
    }

    private CreateRtTokenResponse createRtWithOtt(String relayAuthorization, CreateRtTokenRequest request) {
        OttClaims claims = jwtTokenService.parseAndVerifyOtt(extractRelayToken(relayAuthorization));
        OttConsumeResult consumeResult = jwtTokenService.consumeOneTimeToken(claims.getJti());
        if (consumeResult == OttConsumeResult.NOT_FOUND_OR_EXPIRED) {
            throw new BizException(ErrorCode.TOKEN_NOT_FOUND_OR_EXPIRED);
        }
        if (consumeResult == OttConsumeResult.ALREADY_CONSUMED) {
            throw new BizException(ErrorCode.TOKEN_ALREADY_CONSUMED);
        }
        if (request != null && !StringUtils.isBlank(request.getTunnelId()) && !request.getTunnelId().equals(claims.getTunnelId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "tunnelId does not match OTT");
        }
        Tunnel tunnel = findActiveTunnel(claims.getTunnelId());
        return reusableTokenResponse(jwtTokenService.getOrCreateReusableToken(tunnel));
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

    private String extractRelayToken(String relayAuthorization) {
        String value = relayAuthorization.trim();
        if (value.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return value.substring(BEARER_PREFIX.length()).trim();
        }
        return value;
    }

    private String buildOttJti(String tunnelId, String connId) {
        String connection = StringUtils.isBlank(connId) ? "none" : connId;
        return "ott:" + tunnelId + ":" + connection + ":" + IdUtils.uuid();
    }
}
