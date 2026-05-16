package com.stockpro.gateway.config; 

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter; 

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // 1. Allow local React dev server URLs
        corsConfig.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:5174"
        ));
        
        // 2. Allow all HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        
        // 3. Allow all headers
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        
        // 4. Allow credentials
        corsConfig.setAllowCredentials(true);
        
        // 5. Cache this rule
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // Apply this rule to EVERY route
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsFilter(source); // <-- THE MVC VERSION!
    }
}
