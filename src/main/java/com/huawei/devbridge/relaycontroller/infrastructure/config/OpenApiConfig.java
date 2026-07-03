package com.huawei.devbridge.relaycontroller.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI relayControllerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Relay Controller API")
                .version("v1")
                .description("Control plane APIs for DevBridge relay tunnels. No traffic forwarding is implemented here."));
    }
}
