package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.clouds.wushan.scc.crypto.SccCrypto;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
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
    private final SccCrypto sccCrypto;
    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        String configuredPrivateKey = relayProperties.getJwt().getPrivateKey();
        if (configuredPrivateKey == null || configuredPrivateKey.isBlank()) {
            initEphemeralKeyPair();
            return;
        }
        this.privateKey = parsePrivateKey(sccCrypto.decrypt(configuredPrivateKey));
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
        } catch (Exception exception) {
            throw new BizException(ErrorCode.JWT_KEY_INVALID, "failed to initialize jwt key pair");
        }
    }

    private PrivateKey parsePrivateKey(String configuredValue) {
        try {
            byte[] keyBytes = decodePkcs8(configuredValue);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (Exception exception) {
            throw new BizException(ErrorCode.JWT_KEY_INVALID);
        }
    }

    private byte[] decodePkcs8(String configuredValue) {
        String value = configuredValue.strip().replace("\\n", "\n");
        if (value.startsWith("-----BEGIN PRIVATE KEY-----")) {
            return decodePem(value);
        }

        byte[] decoded = Base64.getMimeDecoder().decode(value);
        String decodedText = new String(decoded, StandardCharsets.US_ASCII).strip();
        return decodedText.startsWith("-----BEGIN PRIVATE KEY-----") ? decodePem(decodedText) : decoded;
    }

    private byte[] decodePem(String pem) {
        String content = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");
        return Base64.getMimeDecoder().decode(content);
    }
}
