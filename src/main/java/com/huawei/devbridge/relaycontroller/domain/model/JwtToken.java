package com.huawei.devbridge.relaycontroller.domain.model;

public record JwtToken(String token, long lifetime, long expiration) {
}
