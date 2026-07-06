package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtSigner {
    private final RelayProperties relayProperties;
    private final JwtKeyProvider jwtKeyProvider;

    public String signToken(Tunnel tunnel, String jti, long ttlSeconds) {
        try {
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(ttlSeconds);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .jwtID(jti)
                    .claim("typ", "TOKEN")
                    .issuer(relayProperties.getJwt().getIssuer())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiresAt))
                    .claim("tunnelId", tunnel.getTunnelId())
                    .claim("tunnelCode", tunnel.getTunnelCode())
                    .claim("namespace", tunnel.getNamespace())
                    .claim("gridname", tunnel.getGridName())
                    .build();
            return sign(claims);
        } catch (Exception exception) {
            throw new BizException(ErrorCode.JWT_GENERATE_FAILED);
        }
    }

    private String sign(JWTClaimsSet claims) throws Exception {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(relayProperties.getJwt().getKeyId())
                .build();
        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new RSASSASigner((RSAPrivateKey) jwtKeyProvider.getPrivateKey()));
        return jwt.serialize();
    }
}
