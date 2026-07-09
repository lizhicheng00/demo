package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;

import com.huawei.devbridge.relaycontroller.infrastructure.config.SccEnvironmentPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SccEnvironmentPostProcessorTest {
    @Test
    void shouldDecryptSecretsBeforePropertiesAreBound() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.password", "{scc}mysql-pass")
                .withProperty("spring.data.redis.password", "{scc}redis-pass")
                .withProperty("relay.jwt.private-key", "{scc}jwt-key")
                .withProperty("server.ssl.key-password", "{scc}server-key-pass")
                .withProperty("server.ssl.key-store-password", "{scc}server-store-pass")
                .withProperty("server.ssl.trust-store-password", "{scc}trust-store-pass");

        new SccEnvironmentPostProcessor().postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("mysql-pass");
        assertThat(environment.getProperty("spring.data.redis.password")).isEqualTo("redis-pass");
        assertThat(environment.getProperty("relay.jwt.private-key")).isEqualTo("jwt-key");
        assertThat(environment.getProperty("server.ssl.key-password")).isEqualTo("server-key-pass");
        assertThat(environment.getProperty("server.ssl.key-store-password")).isEqualTo("server-store-pass");
        assertThat(environment.getProperty("server.ssl.trust-store-password")).isEqualTo("trust-store-pass");
    }
}
