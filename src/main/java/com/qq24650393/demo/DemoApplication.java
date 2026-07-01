package com.qq24650393.demo;

import com.qq24650393.demo.auth.AdminProperties;
import com.qq24650393.demo.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.qq24650393.demo")
@EnableConfigurationProperties({JwtProperties.class, AdminProperties.class})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
