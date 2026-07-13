package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.common.util.Base32Utils;
import com.huawei.us.common.random.UsSecureRandom;
import org.springframework.stereotype.Service;

@Service
public class TunnelCodeGenerator {
    private static final long MAX_40_BIT = (1L << 40) - 1;

    public long generate() {
        return UsSecureRandom.getInstance().nextLong(MAX_40_BIT) + 1;
    }

    public String toTunnelId(long tunnelCode) {
        return Base32Utils.encode40Bit(tunnelCode);
    }
}
