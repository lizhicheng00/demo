package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
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
    public String getOrCreateToken(Tunnel tunnel) {
        long ttlSeconds = resolveTokenTtlSeconds(tunnel);
        String cached = jwtTokenCache.get(tunnel.getTunnelId());
        if (cached != null && !cached.isBlank()) {
            return cached;
        }
        String token = createToken(tunnel);
        jwtTokenCache.set(tunnel.getTunnelId(), token, ttlSeconds);
        return token;
    }

    @Override
    public String createToken(Tunnel tunnel) {
        return jwtSigner.sign(tunnel, resolveTokenTtlSeconds(tunnel));
    }

    @Override
    public void evictToken(String tunnelId) {
        jwtTokenCache.delete(tunnelId);
    }

    @Override
    public Map<String, String> getPublicKeys() {
        return jwtKeyProvider.getPublicKeys();
    }

    private long resolveTokenTtlSeconds(Tunnel tunnel) {
        long configuredTtl = relayProperties.getJwt().getTtlSeconds();
        if (tunnel.getExpiration() == null || tunnel.getExpiration() <= 0) {
            return configuredTtl;
        }
        long remainingSeconds = tunnel.getExpiration() - TimeUtils.nowSeconds();
        if (remainingSeconds <= 0) {
            throw new BizException(ErrorCode.TUNNEL_EXPIRED);
        }
        return Math.min(configuredTtl, remainingSeconds);
    }
}
