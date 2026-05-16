package com.stockpro.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class RolePermissionServiceTest {

    private final RolePermissionService permissions = new RolePermissionService();

    @Test
    void staffCanReadProductsButCannotDeleteProducts() {
        assertThat(permissions.isAllowed(new MockHttpServletRequest("GET", "/products"), "STAFF")).isTrue();
        assertThat(permissions.isAllowed(new MockHttpServletRequest("DELETE", "/products/1"), "STAFF")).isFalse();
    }

    @Test
    void officerCanManageSuppliersButCannotReadReports() {
        assertThat(permissions.isAllowed(new MockHttpServletRequest("POST", "/suppliers"), "OFFICER")).isTrue();
        assertThat(permissions.isAllowed(new MockHttpServletRequest("GET", "/reports/inventory/valuation"), "OFFICER")).isFalse();
    }

    @Test
    void managerCanApprovePurchasesAndReadReports() {
        assertThat(permissions.isAllowed(new MockHttpServletRequest("PUT", "/purchase-orders/1/approve"), "MANAGER")).isTrue();
        assertThat(permissions.isAllowed(new MockHttpServletRequest("GET", "/reports/inventory/valuation"), "MANAGER")).isTrue();
    }
}
