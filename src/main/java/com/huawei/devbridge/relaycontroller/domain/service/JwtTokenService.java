package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.domain.model.CreateOttTokenCommand;
import com.huawei.devbridge.relaycontroller.domain.model.OttClaims;
import com.huawei.devbridge.relaycontroller.domain.model.OttConsumeResult;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import java.util.Map;

public interface JwtTokenService {
    String createOneTimeToken(CreateOttTokenCommand command);

    String getOrCreateReusableToken(Tunnel tunnel);

    String createReusableToken(Tunnel tunnel);

    OttClaims parseAndVerifyOtt(String token);

    OttConsumeResult consumeOneTimeToken(String jti);

    void evictReusableToken(String tunnelId);

    Map<String, String> getPublicKeys();
}
