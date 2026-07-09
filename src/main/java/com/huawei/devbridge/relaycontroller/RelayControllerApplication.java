package com.huawei.devbridge.relaycontroller;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.huawei.devbridge.relaycontroller.infrastructure.persistence.mapper")
@EnableScheduling
@Slf4j
public class RelayControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RelayControllerApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String port = env.getProperty("local.server.port", env.getProperty("server.port", "8080"));
        log.info("Relay Controller started on port {}", port);
    }
}
