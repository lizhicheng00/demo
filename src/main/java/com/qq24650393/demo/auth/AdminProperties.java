package com.qq24650393.demo.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(boolean seedEnabled, String username, String password) {
}
