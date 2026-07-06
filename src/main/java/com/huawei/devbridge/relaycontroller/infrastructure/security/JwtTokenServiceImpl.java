package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.devbridge.relaycontroller.common.util.IdUtils;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.service.JwtTokenService;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.infrastructure.redis.JwtTokenCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {
    private final JwtTokenCache jwtTokenCache;
    private final JwtSigner jwtSigner;
    private final RelayProperties relayProperties;

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
    public void evictReusableToken(String tunnelId) {
        jwtTokenCache.deleteReusableToken(tunnelId);
    }
}
