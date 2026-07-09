package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;

import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.infrastructure.security.SccCrypto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

class SccSecretBindHandlerAdvisorTest {
    @Test
    void shouldDecryptSecretsDuringConfigurationBinding() {
        new ApplicationContextRunner()
                .withUserConfiguration(TestConfig.class)
                .withPropertyValues(
                        "spring.datasource.password={scc}mysql-pass",
                        "spring.data.redis.password={scc}redis-pass",
                        "relay.jwt.private-key={scc}jwt-key",
                        "server.ssl.key-password={scc}key-pass",
                        "server.ssl.key-store-password={scc}store-pass",
                        "server.ssl.trust-store-password={scc}trust-pass")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(DataSourceProperties.class).getPassword()).isEqualTo("mysql-pass");
                    assertThat(context.getBean(RedisProperties.class).getPassword()).isEqualTo("redis-pass");
                    assertThat(context.getBean(RelayProperties.class).getJwt().getPrivateKey()).isEqualTo("jwt-key");

                    ServerProperties serverProperties = context.getBean(ServerProperties.class);
                    assertThat(serverProperties.getSsl().getKeyPassword()).isEqualTo("key-pass");
                    assertThat(serverProperties.getSsl().getKeyStorePassword()).isEqualTo("store-pass");
                    assertThat(serverProperties.getSsl().getTrustStorePassword()).isEqualTo("trust-pass");
                });
    }

    @Test
    void shouldKeepBlankSecretUnchanged() {
        new ApplicationContextRunner()
                .withUserConfiguration(TestConfig.class)
                .withPropertyValues("spring.data.redis.password=")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(RedisProperties.class).getPassword()).isEmpty();
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({
            DataSourceProperties.class,
            RedisProperties.class,
            RelayProperties.class,
            ServerProperties.class
    })
    @Import({SccCrypto.class, SccSecretBindHandlerAdvisor.class})
    static class TestConfig {
    }
}
