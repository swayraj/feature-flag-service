package com.flagservice.feature_flag_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
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
                                     "webhooks, real-time updates, and API key authentication."))
                .addSecurityItem(new SecurityRequirement().addList("X-API-Key"))
                .components(new Components()
                        .addSecuritySchemes("X-API-Key", new SecurityScheme()
                                .name("X-API-Key")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)));
    }
}
