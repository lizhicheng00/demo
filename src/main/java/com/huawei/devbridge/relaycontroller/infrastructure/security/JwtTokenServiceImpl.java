package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.IdUtils;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.domain.model.JwtToken;
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
    public JwtToken getOrCreateToken(Tunnel tunnel) {
        long ttlSeconds = resolveTokenTtlSeconds(tunnel);
        JwtToken cached = jwtTokenCache.getToken(tunnel.getTunnelId());
        if (cached != null && cached.expiresIn() <= ttlSeconds) {
            return cached;
        }
        String token = createToken(tunnel, ttlSeconds);
        jwtTokenCache.setToken(tunnel.getTunnelId(), token, ttlSeconds);
        return new JwtToken(token, ttlSeconds);
    }

    private String createToken(Tunnel tunnel, long ttlSeconds) {
        return jwtSigner.signToken(tunnel, "token:" + tunnel.getTunnelId() + ":" + IdUtils.uuid(),
                ttlSeconds);
    }

    @Override
    public void evictToken(String tunnelId) {
        jwtTokenCache.deleteToken(tunnelId);
    }

    private long resolveTokenTtlSeconds(Tunnel tunnel) {
        long configuredTtl = relayProperties.getJwt().getToken().getTtlSeconds();
        if (tunnel.getExpiration() == null) {
            return configuredTtl;
        }
        long remainingSeconds = tunnel.getExpiration() - TimeUtils.nowSeconds();
        if (remainingSeconds <= 0) {
            throw new BizException(ErrorCode.TUNNEL_EXPIRED);
        }
        return Math.min(configuredTtl, remainingSeconds);
    }
}
