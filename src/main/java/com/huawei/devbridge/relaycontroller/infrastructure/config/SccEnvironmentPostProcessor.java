package com.huawei.devbridge.relaycontroller.infrastructure.config;

import com.huawei.devbridge.relaycontroller.infrastructure.security.SccCrypto;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class SccEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final String PROPERTY_SOURCE_NAME = "sccDecryptedSecrets";
    private static final String[] SECRET_KEYS = {
            "spring.datasource.password",
            "spring.data.redis.password",
            "relay.jwt.private-key",
            "server.ssl.key-password",
            "server.ssl.key-store-password",
            "server.ssl.trust-store-password"
    };

    private final SccCrypto sccCrypto = new SccCrypto();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> decryptedProperties = new LinkedHashMap<>();
        for (String key : SECRET_KEYS) {
            String value = getConfiguredValue(environment, key);
            if (value == null || value.isBlank()) {
                continue;
            }
            String decrypted = sccCrypto.decrypt(value);
            if (!decrypted.equals(value)) {
                decryptedProperties.put(key, decrypted);
            }
        }
        if (!decryptedProperties.isEmpty()) {
            environment.getPropertySources()
                    .addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, decryptedProperties));
        }
    }

    private String getConfiguredValue(ConfigurableEnvironment environment, String key) {
        try {
            return environment.getProperty(key);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
