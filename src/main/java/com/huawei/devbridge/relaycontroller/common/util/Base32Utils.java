package com.huawei.devbridge.relaycontroller.common.util;

public final class Base32Utils {
    private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz234567".toCharArray();
    private static final int BITS_PER_CHAR = 5;

    private Base32Utils() {
    }

    public static String encode40Bit(long value) {
        char[] result = new char[8];
        for (int i = 0; i < result.length; i++) {
            int shift = (result.length - 1 - i) * BITS_PER_CHAR;
            result[i] = ALPHABET[(int) ((value >> shift) & 31)];
        }
        return new String(result);
    }
}
