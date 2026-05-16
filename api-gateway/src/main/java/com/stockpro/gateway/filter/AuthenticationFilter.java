package com.stockpro.gateway.filter;

import com.stockpro.gateway.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class AuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (validator.isSecured.test(request)) {
            // Check for Authorization header
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Rejected request {} {} because Authorization header is missing", request.getMethod(),
                        request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Missing Authorization Header");
                return;
            }

            String token = authHeader.substring(7);
            try {
                jwtUtil.validateToken(token);
                String role = jwtUtil.extractRole(token);
                if (!rolePermissionService.isAllowed(request, role)) {
                    log.warn("Rejected request {} {} because role {} is not allowed", request.getMethod(),
                            request.getRequestURI(), role);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Forbidden: insufficient role");
                    return;
                }
            } catch (Exception e) {
                log.warn("Rejected request {} {} because token is invalid", request.getMethod(),
                        request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid Token: " + e.getMessage());
                return;
            }
        }

        log.debug("Gateway accepted request {} {}", request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}
