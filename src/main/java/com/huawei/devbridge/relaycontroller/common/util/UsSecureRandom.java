package com.huawei.devbridge.relaycontroller.common.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class UsSecureRandom {
    private UsSecureRandom() {
    }

    public static SecureRandom getInstance() throws NoSuchAlgorithmException {
        return SecureRandom.getInstance("DRBG");
    }
}
