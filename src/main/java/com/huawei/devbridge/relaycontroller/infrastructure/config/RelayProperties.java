package com.huawei.devbridge.relaycontroller.infrastructure.config;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "relay")
public class RelayProperties {
    private String domain = "relayprovider.xxx.com";
    private long defaultExpirationSeconds = 259200;
    private Jwt jwt = new Jwt();

    @Data
    public static class Jwt {
        private String issuer = "devbridge";
        private long ttlSeconds = 86400;
        private String keyId = "1";
        private String privateKey;
        private Map<String, String> publicKeys = new LinkedHashMap<>();
    }
}
