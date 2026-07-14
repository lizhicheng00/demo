package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtKeyProvider {
    private final RelayProperties relayProperties;
    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        String configuredPrivateKey = relayProperties.getJwt().getPrivateKey();
        if (configuredPrivateKey == null || configuredPrivateKey.isBlank()) {
            if (!relayProperties.getJwt().isAllowEphemeralKey()) {
                throw new BizException(ErrorCode.JWT_KEY_INVALID, "relay.jwt.private-key is required");
            }
            initEphemeralKeyPair();
            return;
        }
        this.privateKey = parsePrivateKey(configuredPrivateKey);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    private void initEphemeralKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
        } catch (GeneralSecurityException exception) {
            throw new BizException(ErrorCode.JWT_KEY_INVALID, "failed to initialize jwt key pair");
        }
    }

    private PrivateKey parsePrivateKey(String pem) {
        try {
            String content = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] bytes = Base64.getDecoder().decode(content);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new BizException(ErrorCode.JWT_KEY_INVALID);
        }
    }
}
