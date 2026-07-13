package com.huawei.us.common.random;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class UsSecureRandom {
    private static final SecureRandom INSTANCE = createInstance();

    private UsSecureRandom() {
    }

    public static SecureRandom getInstance() {
        return INSTANCE;
    }

    private static SecureRandom createInstance() {
        try {
            return SecureRandom.getInstance("DRBG");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("DRBG secure random is unavailable", exception);
        }
    }
}
