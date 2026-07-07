package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.domain.model.JwtToken;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;

public interface JwtTokenService {
    JwtToken getOrCreateToken(Tunnel tunnel);

    void evictToken(String tunnelId);
}
