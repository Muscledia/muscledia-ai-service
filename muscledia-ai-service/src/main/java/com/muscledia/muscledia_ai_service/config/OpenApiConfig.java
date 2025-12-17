package com.muscledia.muscledia_ai_service.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8084}")
    private String serverPort;

    private static final String BEARER_KEY_SECURITY_SCHEME = "bearer-key";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(BEARER_KEY_SECURITY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_KEY_SECURITY_SCHEME, createAPIKeyScheme()))
                .info(new Info()
                        .title("Muscledia AI Service API")
                        .version("1.0.0")
                        .description(
                                "RESTful API for managing user recommendation requests in the Muscledia platform. "
                                        +
                                        "Authentication is required for personal personal recommendationsand  admin operations.")
                        .contact(new Contact()
                                .name("Muscledia Team")
                                .email("api@muscledia.com")
                                .url("https://muscledia.com")));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("Enter JWT Bearer token in the format: your-jwt-token-here");
    }
}