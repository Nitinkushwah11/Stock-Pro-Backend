package com.stockpro.gateway.filter;

import com.stockpro.gateway.util.JwtUtil;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

class AuthenticationFilterTest {

    private AuthenticationFilter filter;
    private JwtUtil jwtUtil;
    private RolePermissionService rolePermissionService;

    @BeforeEach
    void setUp() {
        filter = new AuthenticationFilter();
        jwtUtil = mock(JwtUtil.class);
        rolePermissionService = mock(RolePermissionService.class);
        ReflectionTestUtils.setField(filter, "validator", new RouteValidator());
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(filter, "rolePermissionService", rolePermissionService);
    }

    @Test
    void missingTokenReturnsUnauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/products");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Missing Authorization Header");
    }

    @Test
    void invalidTokenReturnsUnauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/products");
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        doThrow(new RuntimeException("bad token")).when(jwtUtil).validateToken("bad-token");

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid Token");
    }

    @Test
    void validTokenWithWrongRoleReturnsForbidden() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/products/1");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.extractRole("valid-token")).thenReturn("STAFF");
        when(rolePermissionService.isAllowed(request, "STAFF")).thenReturn(false);

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("Forbidden");
        verify(jwtUtil).validateToken("valid-token");
    }

    @Test
    void validTokenWithAllowedRolePasses() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/products");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(jwtUtil.extractRole("valid-token")).thenReturn("STAFF");
        when(rolePermissionService.isAllowed(request, "STAFF")).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(jwtUtil).validateToken("valid-token");
    }
}
