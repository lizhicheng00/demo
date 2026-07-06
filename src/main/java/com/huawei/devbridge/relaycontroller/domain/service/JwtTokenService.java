package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;

public interface JwtTokenService {
    String getOrCreateReusableToken(Tunnel tunnel);

    String createReusableToken(Tunnel tunnel);

    void evictReusableToken(String tunnelId);
}
