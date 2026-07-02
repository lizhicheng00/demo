package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import java.util.Map;

public interface JwtTokenService {
    String getOrCreateToken(Tunnel tunnel);

    String createToken(Tunnel tunnel);

    void evictToken(String tunnelId);

    Map<String, String> getPublicKeys();
}
