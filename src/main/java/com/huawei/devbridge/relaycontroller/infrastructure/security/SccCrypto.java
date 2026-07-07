package com.huawei.devbridge.relaycontroller.infrastructure.security;

import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SccCrypto {
    private static final String PREFIX = "{scc}";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final RelayProperties relayProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public String encrypt(String value) {
        if (value == null || value.isBlank() || value.startsWith(PREFIX)) {
            return value;
        }
        String key = cryptoKey();
        if (key == null || key.isBlank()) {
            return value;
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(key), new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(payload);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("scc crypto encrypt failed", exception);
        }
    }

    public String decrypt(String value) {
        if (value == null || !value.startsWith(PREFIX)) {
            return value;
        }
        String key = cryptoKey();
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("relay.crypto.key is required to decrypt scc value");
        }
        try {
            byte[] payload = Base64.getUrlDecoder().decode(value.substring(PREFIX.length()));
            byte[] iv = Arrays.copyOfRange(payload, 0, IV_BYTES);
            byte[] encrypted = Arrays.copyOfRange(payload, IV_BYTES, payload.length);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, secretKey(key), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException exception) {
            throw new IllegalStateException("scc crypto decrypt failed", exception);
        }
    }

    private SecretKeySpec secretKey(String key) throws GeneralSecurityException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(digest, KEY_ALGORITHM);
    }

    private String cryptoKey() {
        return relayProperties.getCrypto().getKey();
    }
}
