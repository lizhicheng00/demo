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
    public String getOrCreateToken(Tunnel tunnel) {
        long ttlSeconds = relayProperties.getJwt().getToken().getTtlSeconds();
        String cached = jwtTokenCache.getToken(tunnel.getTunnelId());
        if (cached != null && !cached.isBlank()) {
            return cached;
        }
        String token = createToken(tunnel);
        jwtTokenCache.setToken(tunnel.getTunnelId(), token, ttlSeconds);
        return token;
    }

    @Override
    public String createToken(Tunnel tunnel) {
        return jwtSigner.signToken(tunnel, "token:" + tunnel.getTunnelId() + ":" + IdUtils.uuid(), relayProperties.getJwt().getToken().getTtlSeconds());
    }

    @Override
    public void evictToken(String tunnelId) {
        jwtTokenCache.deleteToken(tunnelId);
    }
}
