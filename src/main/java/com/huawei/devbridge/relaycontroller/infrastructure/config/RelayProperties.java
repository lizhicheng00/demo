package com.huawei.devbridge.relaycontroller.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "relay")
public class RelayProperties {
    private String domain = "myhuaweicloud.com";
    private String region = "region-a";
    private int defaultExpirationHours = 72;
    private Crypto crypto = new Crypto();
    private Jwt jwt = new Jwt();

    @Data
    public static class Crypto {
        private String key;
    }

    @Data
    public static class Jwt {
        private String issuer = "devbridge";
        private String keyId = "1";
        private String privateKey;
        private TokenTtl token = new TokenTtl(86400);
    }

    @Data
    public static class TokenTtl {
        private long ttlSeconds;

        public TokenTtl() {
        }

        public TokenTtl(long ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }
    }
}
