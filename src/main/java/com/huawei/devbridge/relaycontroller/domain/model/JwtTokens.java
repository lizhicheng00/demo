package com.huawei.devbridge.relaycontroller.domain.model;

public record JwtTokens(String connect, String host, long expiresIn) {
}
