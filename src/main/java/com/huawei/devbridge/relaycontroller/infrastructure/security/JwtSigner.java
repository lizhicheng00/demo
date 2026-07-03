package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.StringUtils;
import com.huawei.devbridge.relaycontroller.domain.model.CreateOttTokenCommand;
import com.huawei.devbridge.relaycontroller.domain.model.OttClaims;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtSigner {
    private final RelayProperties relayProperties;
    private final JwtKeyProvider jwtKeyProvider;

    public String signOneTimeToken(CreateOttTokenCommand command, long ttlSeconds) {
        try {
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(ttlSeconds);
            Tunnel tunnel = command.getTunnel();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .jwtID(command.getJti())
                    .claim("typ", "OTT")
                    .issuer(relayProperties.getJwt().getIssuer())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiresAt))
                    .claim("tunnelId", tunnel.getTunnelId())
                    .claim("tunnelCode", tunnel.getTunnelCode())
                    .claim("namespace", tunnel.getNamespace())
                    .claim("gridname", tunnel.getGridName())
                    .claim("connId", command.getConnId())
                    .claim("callbackUrl", command.getCallbackUrl())
                    .claim("requestPort", command.getRequestPort())
                    .build();
            return sign(claims);
        } catch (Exception exception) {
            throw new BizException(ErrorCode.JWT_GENERATE_FAILED);
        }
    }

    public String signReusableToken(Tunnel tunnel, String jti, long ttlSeconds) {
        try {
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(ttlSeconds);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .jwtID(jti)
                    .claim("typ", "RT")
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

    public OttClaims parseAndVerifyOtt(String token) {
        try {
            JWTClaimsSet claims = parseAndVerify(token);
            if (!"OTT".equals(claims.getStringClaim("typ"))) {
                throw new BizException(ErrorCode.TOKEN_INVALID);
            }
            return OttClaims.builder()
                    .jti(claims.getJWTID())
                    .tunnelId(claims.getStringClaim("tunnelId"))
                    .tunnelCode(claims.getLongClaim("tunnelCode"))
                    .namespace(claims.getStringClaim("namespace"))
                    .gridName(claims.getStringClaim("gridname"))
                    .connId(claims.getStringClaim("connId"))
                    .callbackUrl(claims.getStringClaim("callbackUrl"))
                    .requestPort(toInteger(claims.getClaim("requestPort")))
                    .expiresAt(claims.getExpirationTime().toInstant().getEpochSecond())
                    .build();
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
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

    private JWTClaimsSet parseAndVerify(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            RSAPublicKey publicKey = parsePublicKey(resolvePublicKey(jwt.getHeader().getKeyID()));
            if (!jwt.verify(new RSASSAVerifier(publicKey))) {
                throw new BizException(ErrorCode.TOKEN_INVALID);
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (!relayProperties.getJwt().getIssuer().equals(claims.getIssuer())) {
                throw new BizException(ErrorCode.TOKEN_INVALID);
            }
            Date expirationTime = claims.getExpirationTime();
            if (expirationTime == null || !expirationTime.after(new Date())) {
                throw new BizException(ErrorCode.TOKEN_NOT_FOUND_OR_EXPIRED);
            }
            return claims;
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }
    }

    private String resolvePublicKey(String keyId) {
        Map<String, String> publicKeys = jwtKeyProvider.getPublicKeys();
        if (!StringUtils.isBlank(keyId) && publicKeys.containsKey(keyId)) {
            return publicKeys.get(keyId);
        }
        if (publicKeys.size() == 1) {
            return publicKeys.values().iterator().next();
        }
        throw new BizException(ErrorCode.TOKEN_INVALID);
    }

    private RSAPublicKey parsePublicKey(String pem) throws Exception {
        String content = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] bytes = Base64.getDecoder().decode(content);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }
}
