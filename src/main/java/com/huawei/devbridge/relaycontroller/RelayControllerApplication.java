package com.huawei.devbridge.relaycontroller;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.huawei.devbridge.relaycontroller.infrastructure.persistence.mapper")
public class RelayControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RelayControllerApplication.class, args);
    }
}
