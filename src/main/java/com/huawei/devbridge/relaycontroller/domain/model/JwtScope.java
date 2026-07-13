package com.huawei.devbridge.relaycontroller.domain.model;

public enum JwtScope {
    CONNECT("connect"),
    HOST("host");

    private final String value;

    JwtScope(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
