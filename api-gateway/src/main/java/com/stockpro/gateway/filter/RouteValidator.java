package com.stockpro.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/auth/register",
            "/auth/login",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/auth/validate",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/eureka",
            "/actuator"
    );

    public Predicate<jakarta.servlet.http.HttpServletRequest> isSecured =
            request -> !"/".equals(request.getRequestURI()) && openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getRequestURI().contains(uri));

}
