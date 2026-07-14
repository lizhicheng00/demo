package com.huawei.devbridge.relaycontroller.domain.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum TunnelProtocol {
    HTTP("http"),
    HTTPS("https"),
    AUTO("auto");

    @EnumValue
    private final String value;

    TunnelProtocol(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static TunnelProtocol fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(protocol -> protocol.value.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported tunnel protocol: " + value));
    }
}
