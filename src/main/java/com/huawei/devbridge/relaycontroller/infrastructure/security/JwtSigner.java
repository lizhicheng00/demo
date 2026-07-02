package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.IdUtils;
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

    public String sign(Tunnel tunnel) {
        try {
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(relayProperties.getJwt().getTtlSeconds());
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .keyID(relayProperties.getJwt().getKeyId())
                    .build();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .jwtID(IdUtils.uuid())
                    .issuer(relayProperties.getJwt().getIssuer())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiresAt))
                    .claim("tunnelId", tunnel.getTunnelid())
                    .claim("tunnelCode", tunnel.getTunnelcode())
                    .claim("namespace", tunnel.getNamespace())
                    .claim("gridname", tunnel.getGridname())
                    .build();
            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new RSASSASigner((RSAPrivateKey) jwtKeyProvider.getPrivateKey()));
            return jwt.serialize();
        } catch (Exception exception) {
            throw new BizException(ErrorCode.JWT_GENERATE_FAILED);
        }
    }
}
