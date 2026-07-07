package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.common.util.Base32Utils;
import com.huawei.devbridge.relaycontroller.common.util.UsSecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;

@Service
public class TunnelCodeGenerator {
    private static final long MAX_40_BIT = (1L << 40) - 1;
    private final SecureRandom secureRandom;

    public TunnelCodeGenerator() {
        try {
            this.secureRandom = UsSecureRandom.getInstance();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("failed to initialize secure random", exception);
        }
    }

    public long generate() {
        return secureRandom.nextLong(MAX_40_BIT) + 1;
    }

    public String toTunnelId(long tunnelCode) {
        return Base32Utils.encode40Bit(tunnelCode);
    }
}
