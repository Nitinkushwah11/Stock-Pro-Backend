package com.stockpro.warehouse.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignAuthConfig {

    @Bean
    RequestInterceptor authorizationHeaderForwarder() {
        return template -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
                String authorization = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (authorization != null && !authorization.isBlank()) {
                    template.header(HttpHeaders.AUTHORIZATION, authorization);
                }
            }
        };
    }
}
