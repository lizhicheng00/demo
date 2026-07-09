package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.huawei.devbridge.relaycontroller.infrastructure.config.SccSecretPropertyConfiguration;
import com.huawei.devbridge.relaycontroller.infrastructure.config.SccSecretPropertyPostProcessor;
import com.huawei.devbridge.relaycontroller.infrastructure.security.SccCrypto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.env.MockEnvironment;

class SccSecretPropertyPostProcessorTest {
    private static final String SCC_PROPERTY_SOURCE = "sccDecryptedSecrets";

    @Test
    void shouldDecryptSecretsBeforePropertiesAreBound() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.password", "{scc}mysql-pass")
                .withProperty("spring.data.redis.password", "{scc}redis-pass")
                .withProperty("relay.jwt.private-key", "{scc}jwt-key")
                .withProperty("server.ssl.key-password", "{scc}server-key-pass")
                .withProperty("server.ssl.key-store-password", "{scc}server-store-pass")
                .withProperty("server.ssl.trust-store-password", "{scc}trust-store-pass");

        postProcess(environment);

        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("mysql-pass");
        assertThat(environment.getProperty("spring.data.redis.password")).isEqualTo("redis-pass");
        assertThat(environment.getProperty("relay.jwt.private-key")).isEqualTo("jwt-key");
        assertThat(environment.getProperty("server.ssl.key-password")).isEqualTo("server-key-pass");
        assertThat(environment.getProperty("server.ssl.key-store-password")).isEqualTo("server-store-pass");
        assertThat(environment.getProperty("server.ssl.trust-store-password")).isEqualTo("trust-store-pass");
    }

    @Test
    void shouldKeepBlankSecretUnchanged() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.data.redis.password", "");

        postProcess(environment);

        assertThat(environment.getPropertySources().contains(SCC_PROPERTY_SOURCE)).isFalse();
        assertThat(environment.getProperty("spring.data.redis.password")).isEmpty();
    }

    @Test
    void shouldIgnoreUnresolvedSecretPlaceholder() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("server.ssl.key-store-password", "${SERVER_SSL_KEY_STORE_PASSWORD}");

        postProcess(environment);

        assertThat(environment.getPropertySources().contains(SCC_PROPERTY_SOURCE)).isFalse();
    }

    @Test
    void shouldBeCreatedBySpringWithInjectedCrypto() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            TestPropertyValues.of("spring.data.redis.password={scc}redis-pass")
                    .applyTo(context);
            context.register(SccCrypto.class, SccSecretPropertyConfiguration.class);

            context.refresh();

            assertThat(context.getEnvironment().getProperty("spring.data.redis.password")).isEqualTo("redis-pass");
        }
    }

    private void postProcess(MockEnvironment environment) {
        SccSecretPropertyPostProcessor postProcessor = new SccSecretPropertyPostProcessor(new SccCrypto());
        postProcessor.setEnvironment(environment);
        postProcessor.postProcessBeanFactory(mock(ConfigurableListableBeanFactory.class));
    }
}
