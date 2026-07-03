package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.devbridge.relaycontroller.common.util.IdUtils;
import com.huawei.devbridge.relaycontroller.domain.model.CreateOttTokenCommand;
import com.huawei.devbridge.relaycontroller.domain.model.OttClaims;
import com.huawei.devbridge.relaycontroller.domain.model.OttConsumeResult;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.service.JwtTokenService;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.infrastructure.redis.JwtTokenCache;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {
    private final JwtTokenCache jwtTokenCache;
    private final JwtSigner jwtSigner;
    private final JwtKeyProvider jwtKeyProvider;
    private final RelayProperties relayProperties;

    @Override
    public String createOneTimeToken(CreateOttTokenCommand command) {
        long ttlSeconds = relayProperties.getJwt().getOtt().getTtlSeconds();
        String token = jwtSigner.signOneTimeToken(command, ttlSeconds);
        jwtTokenCache.setOneTimeToken(toOttClaims(command, ttlSeconds), ttlSeconds);
        return token;
    }

    @Override
    public String getOrCreateReusableToken(Tunnel tunnel) {
        long ttlSeconds = relayProperties.getJwt().getRt().getTtlSeconds();
        String cached = jwtTokenCache.getReusableToken(tunnel.getTunnelId());
        if (cached != null && !cached.isBlank()) {
            return cached;
        }
        String token = createReusableToken(tunnel);
        jwtTokenCache.setReusableToken(tunnel.getTunnelId(), token, ttlSeconds);
        return token;
    }

    @Override
    public String createReusableToken(Tunnel tunnel) {
        return jwtSigner.signReusableToken(tunnel, "rt:" + tunnel.getTunnelId() + ":" + IdUtils.uuid(), relayProperties.getJwt().getRt().getTtlSeconds());
    }

    @Override
    public OttClaims parseAndVerifyOtt(String token) {
        return jwtSigner.parseAndVerifyOtt(token);
    }

    @Override
    public OttConsumeResult consumeOneTimeToken(String jti) {
        return jwtTokenCache.consumeOneTimeToken(jti, relayProperties.getJwt().getOtt().getConsumedTtlSeconds());
    }

    @Override
    public void evictReusableToken(String tunnelId) {
        jwtTokenCache.deleteReusableToken(tunnelId);
    }

    @Override
    public Map<String, String> getPublicKeys() {
        return jwtKeyProvider.getPublicKeys();
    }

    private OttClaims toOttClaims(CreateOttTokenCommand command, long ttlSeconds) {
        Tunnel tunnel = command.getTunnel();
        return OttClaims.builder()
                .jti(command.getJti())
                .tunnelId(tunnel.getTunnelId())
                .tunnelCode(tunnel.getTunnelCode())
                .namespace(tunnel.getNamespace())
                .gridName(tunnel.getGridName())
                .connId(command.getConnId())
                .callbackUrl(command.getCallbackUrl())
                .requestPort(command.getRequestPort())
                .expiresAt(System.currentTimeMillis() / 1000 + ttlSeconds)
                .build();
    }
}
