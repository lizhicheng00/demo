package com.huawei.devbridge.relaycontroller.infrastructure.config;

import com.huawei.devbridge.relaycontroller.infrastructure.security.SccCrypto;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;

public class SccSecretPropertyPostProcessor implements BeanFactoryPostProcessor, EnvironmentAware, Ordered {
    static final String PROPERTY_SOURCE_NAME = "sccDecryptedSecrets";
    private static final String[] SECRET_KEYS = {
            "spring.datasource.password",
            "spring.data.redis.password",
            "relay.jwt.private-key",
            "server.ssl.key-password",
            "server.ssl.key-store-password",
            "server.ssl.trust-store-password"
    };

    private final SccCrypto sccCrypto;
    private ConfigurableEnvironment environment;

    public SccSecretPropertyPostProcessor(SccCrypto sccCrypto) {
        this.sccCrypto = sccCrypto;
    }

    @Override
    public void setEnvironment(Environment environment) {
        if (environment instanceof ConfigurableEnvironment configurableEnvironment) {
            this.environment = configurableEnvironment;
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (environment == null) {
            return;
        }
        Map<String, Object> decryptedProperties = new LinkedHashMap<>();
        for (String key : SECRET_KEYS) {
            decryptSecret(key, decryptedProperties);
        }
        if (!decryptedProperties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, decryptedProperties));
        }
    }

    private void decryptSecret(String key, Map<String, Object> decryptedProperties) {
        String value = getConfiguredValue(key);
        if (value == null || value.isBlank()) {
            return;
        }
        String decrypted = sccCrypto.decrypt(value);
        if (!decrypted.equals(value)) {
            decryptedProperties.put(key, decrypted);
        }
    }

    private String getConfiguredValue(String key) {
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
