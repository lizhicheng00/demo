package com.huawei.devbridge.relaycontroller.common.util;

public final class StringUtils {
    private StringUtils() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }
}
