package com.stockpro.auth.config; 

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("StockPro Auth Service API")
                        .description("Authentication, Authorization, and User Management APIs")
                        .version("v1.0.0"));
    }
}