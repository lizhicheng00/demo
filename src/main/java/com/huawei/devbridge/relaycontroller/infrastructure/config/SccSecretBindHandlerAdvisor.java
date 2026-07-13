package com.huawei.devbridge.relaycontroller.infrastructure.config;

import com.huawei.clouds.wushan.scc.crypto.SccCrypto;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindHandlerAdvisor;
import org.springframework.boot.context.properties.bind.AbstractBindHandler;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SccSecretBindHandlerAdvisor implements ConfigurationPropertiesBindHandlerAdvisor {
    private static final Set<String> SECRET_PROPERTY_NAMES = Set.of(
            "spring.datasource.password",
            "spring.data.redis.password",
            "relay.jwt.private-key",
            "spring.ssl.bundle.jks.mtls.keystore.password",
            "spring.ssl.bundle.jks.mtls.truststore.password");

    private final SccCrypto sccCrypto;

    @Override
    public BindHandler apply(BindHandler bindHandler) {
        return new AbstractBindHandler(bindHandler) {
            @Override
            public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context,
                    Object result) {
                Object boundValue = super.onSuccess(name, target, context, result);
                return decryptIfNeeded(name, boundValue);
            }
        };
    }

    private Object decryptIfNeeded(ConfigurationPropertyName name, Object value) {
        if (!(value instanceof String text) || text.isBlank() || !SECRET_PROPERTY_NAMES.contains(name.toString())) {
            return value;
        }
        return sccCrypto.decrypt(text);
    }
}
