package com.huawei.devbridge.relaycontroller.infrastructure.config;

import com.huawei.devbridge.relaycontroller.infrastructure.security.SccCrypto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SccSecretPropertyConfiguration {
    @Bean
    public static SccSecretPropertyPostProcessor sccSecretPropertyPostProcessor(SccCrypto sccCrypto) {
        return new SccSecretPropertyPostProcessor(sccCrypto);
    }
}
