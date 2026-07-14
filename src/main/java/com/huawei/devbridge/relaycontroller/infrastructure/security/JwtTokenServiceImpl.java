package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.domain.model.JwtScope;
import com.huawei.devbridge.relaycontroller.domain.model.JwtToken;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.service.JwtTokenService;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {
    private final JwtSigner jwtSigner;
    private final RelayProperties relayProperties;

    @Override
    public JwtToken issueToken(Tunnel tunnel, JwtScope scope) {
        long issuedAt = TimeUtils.nowSeconds();
        long expiration = resolveTokenExpiration(tunnel, issuedAt);
        long lifetime = expiration - issuedAt;
        String token = jwtSigner.signToken(tunnel, scope, issuedAt, expiration);
        return new JwtToken(token, lifetime, expiration);
    }

    private long resolveTokenExpiration(Tunnel tunnel, long issuedAt) {
        long configuredTtl = relayProperties.getJwt().getToken().getTtlSeconds();
        if (configuredTtl <= 0) {
            throw new BizException(ErrorCode.JWT_GENERATE_FAILED);
        }
        if (tunnel.getExpiration() == null) {
            return issuedAt + configuredTtl;
        }
        long remainingSeconds = tunnel.getExpiration() - issuedAt;
        if (remainingSeconds <= 0) {
            throw new BizException(ErrorCode.TUNNEL_EXPIRED);
        }
        return issuedAt + Math.min(configuredTtl, remainingSeconds);
    }
}
