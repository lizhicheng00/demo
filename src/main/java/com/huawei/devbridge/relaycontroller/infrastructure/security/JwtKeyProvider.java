package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.clouds.wushan.scc.crypto.SccCrypto;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtKeyProvider {
    private static final int MIN_RSA_KEY_BITS = 2048;

    private final RelayProperties relayProperties;
    private final SccCrypto sccCrypto;
    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        String configuredPrivateKey = relayProperties.getJwt().getPrivateKey();
        if (configuredPrivateKey == null || configuredPrivateKey.isBlank()) {
            throw new BizException(ErrorCode.JWT_KEY_INVALID, "jwt private key is required");
        }
        this.privateKey = requireStrongRsaKey(parsePrivateKey(sccCrypto.decrypt(configuredPrivateKey)));
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
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

    private PrivateKey requireStrongRsaKey(PrivateKey key) {
        if (!(key instanceof RSAPrivateKey rsaKey)
                || rsaKey.getModulus().bitLength() < MIN_RSA_KEY_BITS) {
            throw new BizException(ErrorCode.JWT_KEY_INVALID, "jwt RSA key must be at least 2048 bits");
        }
        return key;
    }
}
