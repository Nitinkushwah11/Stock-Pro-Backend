package com.stockpro.gateway.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class RolePermissionService {

    private static final Set<String> ALL_ROLES = Set.of("ADMIN", "MANAGER", "OFFICER", "STAFF");
    private static final Set<String> ADMIN_MANAGER = Set.of("ADMIN", "MANAGER");
    private static final Set<String> ADMIN_OFFICER = Set.of("ADMIN", "OFFICER");
    private static final Set<String> OPS_ROLES = Set.of("ADMIN", "MANAGER", "OFFICER");

    private record PermissionRule(String method, String pathPrefix, Set<String> roles) {
        boolean matches(String requestMethod, String path) {
            return ("*".equals(method) || method.equalsIgnoreCase(requestMethod)) && path.startsWith(pathPrefix);
        }
    }

    private static final List<PermissionRule> RULES = List.of(
            new PermissionRule("*", "/auth/users", Set.of("ADMIN")),
            new PermissionRule("*", "/auth/deactivate", Set.of("ADMIN")),
            new PermissionRule("*", "/auth/role", Set.of("ADMIN")),
            new PermissionRule("*", "/auth/me", ALL_ROLES),
            new PermissionRule("*", "/auth/profile", ALL_ROLES),
            new PermissionRule("*", "/auth/password", ALL_ROLES),

            new PermissionRule("GET", "/products", ALL_ROLES),
            new PermissionRule("*", "/products", ADMIN_MANAGER),

            new PermissionRule("GET", "/warehouse", ALL_ROLES),
            new PermissionRule("POST", "/warehouse/stock", OPS_ROLES),
            new PermissionRule("*", "/warehouse", ADMIN_MANAGER),

            new PermissionRule("GET", "/purchase-orders", OPS_ROLES),
            new PermissionRule("POST", "/purchase-orders", ADMIN_OFFICER),
            new PermissionRule("PUT", "/purchase-orders", ADMIN_MANAGER),
            new PermissionRule("POST", "/purchase-orders/", ADMIN_OFFICER),

            new PermissionRule("GET", "/payments", ALL_ROLES),
            new PermissionRule("*", "/payments", Set.of("ADMIN")),

            new PermissionRule("GET", "/suppliers", ADMIN_OFFICER),
            new PermissionRule("POST", "/suppliers", ADMIN_OFFICER),
            new PermissionRule("PUT", "/suppliers", ADMIN_OFFICER),
            new PermissionRule("DELETE", "/suppliers", Set.of("ADMIN")),

            new PermissionRule("GET", "/movements", ALL_ROLES),
            new PermissionRule("POST", "/movements", OPS_ROLES),

            new PermissionRule("GET", "/alerts/recipient", ALL_ROLES),
            new PermissionRule("PUT", "/alerts/recipient", ALL_ROLES),
            new PermissionRule("PUT", "/alerts/", ALL_ROLES),

            new PermissionRule("*", "/reports", ADMIN_MANAGER),
            new PermissionRule("*", "/alerts", ADMIN_MANAGER)
    );

    public boolean isAllowed(HttpServletRequest request, String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String trimmedRole = role.trim().toUpperCase(Locale.ROOT);
        String normalizedRole = trimmedRole.startsWith("ROLE_") ? trimmedRole.substring(5) : trimmedRole;
        if ("ADMIN".equals(normalizedRole)) {
            return true;
        }
        return RULES.stream()
                .filter(rule -> rule.matches(request.getMethod(), request.getRequestURI()))
                .findFirst()
                .map(rule -> rule.roles().contains(normalizedRole))
                .orElse(false);
    }
}

