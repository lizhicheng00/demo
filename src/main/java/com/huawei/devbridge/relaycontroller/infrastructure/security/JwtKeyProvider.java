package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtKeyProvider {
    private final RelayProperties relayProperties;
    private PrivateKey privateKey;
    private final Map<String, String> publicKeys = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        String configuredPrivateKey = relayProperties.getJwt().getPrivateKey();
        if (configuredPrivateKey == null || configuredPrivateKey.isBlank()) {
            initEphemeralKeyPair();
            return;
        }
        this.privateKey = parsePrivateKey(configuredPrivateKey);
        this.publicKeys.putAll(relayProperties.getJwt().getPublicKeys());
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public Map<String, String> getPublicKeys() {
        return publicKeys;
    }

    private void initEphemeralKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKeys.put(relayProperties.getJwt().getKeyId(), toPemPublicKey(keyPair.getPublic()));
        } catch (Exception exception) {
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
        } catch (Exception exception) {
            throw new BizException(ErrorCode.JWT_KEY_INVALID);
        }
    }

    private String toPemPublicKey(PublicKey publicKey) {
        String encoded = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----";
    }
}
