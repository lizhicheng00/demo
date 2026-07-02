package com.huawei.devbridge.relaycontroller.infrastructure.security;

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
    private final PublicKeyConfigProvider publicKeyConfigProvider;
    private final RelayProperties relayProperties;

    @Override
    public String getOrCreateToken(Tunnel tunnel) {
        String cached = jwtTokenCache.get(tunnel.getTunnelid());
        if (cached != null && !cached.isBlank()) {
            return cached;
        }
        String token = createToken(tunnel);
        jwtTokenCache.set(tunnel.getTunnelid(), token, relayProperties.getJwt().getTtlSeconds());
        return token;
    }

    @Override
    public String createToken(Tunnel tunnel) {
        return jwtSigner.sign(tunnel);
    }

    @Override
    public void evictToken(String tunnelId) {
        jwtTokenCache.delete(tunnelId);
    }

    @Override
    public Map<String, String> getPublicKeys() {
        return publicKeyConfigProvider.getPublicKeys();
    }
}
