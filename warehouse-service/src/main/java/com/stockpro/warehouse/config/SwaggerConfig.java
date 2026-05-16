package com.stockpro.warehouse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI stockProOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                .title("StockPro Warehouse Service API")
                .description("API Documentation for Warehouse Management")
                .version("v1.0"));
    }
}