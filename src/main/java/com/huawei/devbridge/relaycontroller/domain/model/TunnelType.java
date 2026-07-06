package com.huawei.devbridge.relaycontroller.domain.model;

import java.util.Arrays;

public enum TunnelType {
    BRIDGE("bridge"),
    ENV("env");

    private final String value;

    TunnelType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static boolean supports(String value) {
        return Arrays.stream(values()).anyMatch(type -> type.value.equals(value));
    }
}
