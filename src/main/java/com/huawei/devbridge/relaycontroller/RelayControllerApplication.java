package com.huawei.devbridge.relaycontroller;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
@MapperScan("com.huawei.devbridge.relaycontroller.infrastructure.persistence.mapper")
public class RelayControllerApplication {

    private static final Logger log = LoggerFactory.getLogger(RelayControllerApplication.class);

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
