package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.domain.model.JwtTokens;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;

public interface JwtTokenService {
    JwtTokens getOrCreateTokens(Tunnel tunnel);

    void evictToken(String tunnelId);
}
