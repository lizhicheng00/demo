package com.huawei.devbridge.relaycontroller.infrastructure.security;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicKeyConfigProvider {
    private final JwtKeyProvider jwtKeyProvider;

    public Map<String, String> getPublicKeys() {
        return jwtKeyProvider.getPublicKeys();
    }
}
