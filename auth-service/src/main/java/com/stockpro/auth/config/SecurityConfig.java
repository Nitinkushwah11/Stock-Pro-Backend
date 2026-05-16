package com.stockpro.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public WebSecurityCustomizer swaggerWebSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
            antMatcher("/v3/api-docs"),
            antMatcher("/v3/api-docs/**"),
            antMatcher("/v3/api-docs.yaml"),
            antMatcher("/swagger-ui/**"),
            antMatcher("/swagger-ui.html"),
            antMatcher("/webjars/**"),
            antMatcher("/swagger-resources/**")
        );
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    // ✅ CHAIN 1 — Handles Swagger + public paths FIRST, no OAuth involved at all
    @Bean
    @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatchers(matchers -> matchers.requestMatchers(
                    antMatcher("/v3/api-docs"),
                    antMatcher("/v3/api-docs/**"),
                    antMatcher("/v3/api-docs.yaml"),
                    antMatcher("/swagger-ui/**"),
                    antMatcher("/swagger-ui.html"),
                    antMatcher("/webjars/**"),
                    antMatcher("/auth/register"),
                    antMatcher("/auth/login"),
                    antMatcher("/auth/forgot-password"),
                    antMatcher("/auth/reset-password"),
                    antMatcher("/actuator/**")
                )
            )
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }

    // ✅ CHAIN 2 — Handles everything else with OAuth2 + JWT
    @Bean
    @Order(2)
    public SecurityFilterChain securedFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                    antMatcher("/v3/api-docs"),
                    antMatcher("/v3/api-docs/**"),
                    antMatcher("/v3/api-docs.yaml"),
                    antMatcher("/swagger-ui/**"),
                    antMatcher("/swagger-ui.html"),
                    antMatcher("/webjars/**"),
                    antMatcher("/swagger-resources/**"),
                    antMatcher("/actuator/**")
                ).permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .successHandler(oAuth2LoginSuccessHandler)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
