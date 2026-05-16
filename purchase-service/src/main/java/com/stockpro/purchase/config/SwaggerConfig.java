package com.stockpro.purchase.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI purchaseServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StockPro Purchase Order Service API")
                        .description("Handles procurement lifecycle, line items, and PO statuses.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("StockPro Engineering Team")
                                .email("admin@stockpro.com")));
    }
}