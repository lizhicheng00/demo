package com.huawei.devbridge.relaycontroller.infrastructure.config;

import com.huawei.devbridge.relaycontroller.infrastructure.security.SccCrypto;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSourcePasswordDecryptor implements BeanPostProcessor {
    private final SccCrypto sccCrypto;

    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof HikariDataSource dataSource) {
            dataSource.setPassword(sccCrypto.decrypt(dataSource.getPassword()));
        }
        return bean;
    }
}
