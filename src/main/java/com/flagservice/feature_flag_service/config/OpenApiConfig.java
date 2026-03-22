package com.flagservice.feature_flag_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Feature Flag Service")
                        .version("1.0.0")
                        .description("API for managing feature flags with rollout control, " +
                                     "webhooks, real-time updates, and API key authentication."));
    }
}
