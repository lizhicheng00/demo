package com.huawei.devbridge.relaycontroller.common.util;

import java.util.Locale;
import org.apache.commons.codec.binary.Base32;

public final class Base32Utils {
    private static final Base32 BASE32 = new Base32();
    private static final int BYTE_MASK = 0xFF;
    private static final long MAX_40_BIT = (1L << 40) - 1;

    private Base32Utils() {
    }

    public static String encode40Bit(long value) {
        if (value < 0 || value > MAX_40_BIT) {
            throw new IllegalArgumentException("value must fit in 40 bits");
        }
        byte[] bytes = new byte[] {
                (byte) ((value >> 32) & BYTE_MASK),
                (byte) ((value >> 24) & BYTE_MASK),
                (byte) ((value >> 16) & BYTE_MASK),
                (byte) ((value >> 8) & BYTE_MASK),
                (byte) (value & BYTE_MASK)
        };
        return BASE32.encodeToString(bytes).replace("=", "").toLowerCase(Locale.ENGLISH);
    }
}
