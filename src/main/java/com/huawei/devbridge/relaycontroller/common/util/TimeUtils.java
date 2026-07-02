package com.huawei.devbridge.relaycontroller.common.util;

import java.time.Instant;

public final class TimeUtils {
    private TimeUtils() {
    }

    public static long nowSeconds() {
        return Instant.now().getEpochSecond();
    }
}
