package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.domain.model.JwtScope;
import com.huawei.devbridge.relaycontroller.domain.model.JwtToken;
import com.huawei.devbridge.relaycontroller.domain.model.JwtTokens;
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
    public JwtTokens getOrCreateTokens(Tunnel tunnel) {
        long ttlSeconds = resolveTokenTtlSeconds(tunnel);
        JwtToken connect = jwtTokenCache.getToken(tunnel.getTunnelId(), JwtScope.CONNECT);
        JwtToken host = jwtTokenCache.getToken(tunnel.getTunnelId(), JwtScope.HOST);
        if (isUsable(connect, ttlSeconds) && isUsable(host, ttlSeconds)) {
            return new JwtTokens(connect.token(), host.token(), Math.min(connect.expiresIn(), host.expiresIn()));
        }
        String connectToken = jwtSigner.signToken(tunnel, JwtScope.CONNECT, ttlSeconds);
        String hostToken = jwtSigner.signToken(tunnel, JwtScope.HOST, ttlSeconds);
        jwtTokenCache.setToken(tunnel.getTunnelId(), JwtScope.CONNECT, connectToken, ttlSeconds);
        jwtTokenCache.setToken(tunnel.getTunnelId(), JwtScope.HOST, hostToken, ttlSeconds);
        return new JwtTokens(connectToken, hostToken, ttlSeconds);
    }

    private static boolean isUsable(JwtToken token, long ttlSeconds) {
        return token != null && token.expiresIn() <= ttlSeconds;
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
