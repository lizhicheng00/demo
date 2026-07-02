package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.common.util.HexUtils;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;

@Service
public class TunnelCodeGenerator {
    private static final long MAX_40_BIT = (1L << 40) - 1;
    private final SecureRandom secureRandom = new SecureRandom();

    public long generate() {
        return secureRandom.nextLong(MAX_40_BIT + 1);
    }

    public String toTunnelId(long tunnelCode) {
        return HexUtils.toFixedWidthHex(tunnelCode, 10);
    }
}
