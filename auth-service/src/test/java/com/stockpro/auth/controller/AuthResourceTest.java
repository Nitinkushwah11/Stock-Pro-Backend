package com.stockpro.auth.controller;

import com.stockpro.auth.dto.AuthResponseDTO;
import com.stockpro.auth.dto.ForgotPasswordRequestDTO;
import com.stockpro.auth.dto.LoginRequestDTO;
import com.stockpro.auth.dto.RegisterRequestDTO;
import com.stockpro.auth.dto.ResetPasswordRequestDTO;
import com.stockpro.auth.entity.User;
import com.stockpro.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthResourceTest {

    @Mock
    private AuthService service;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthResource authResource;

    @Test
    void delegatesAllAuthEndpoints() {
        User user = new User();
        user.setUserId(1L);
        user.setFullName("Test User");
        user.setEmail("user@example.com");
        user.setRole("ADMIN");
        AuthResponseDTO auth = AuthResponseDTO.builder().userId(1L).email("user@example.com").role("ADMIN").build();

        RegisterRequestDTO register = new RegisterRequestDTO();
        register.setFullName("Test User");
        register.setEmail("user@example.com");
        register.setPassword("secret123");
        register.setPhone("9876543210");
        register.setRole("ADMIN");
        register.setDepartment("IT");
        LoginRequestDTO login = new LoginRequestDTO();
        login.setEmail("user@example.com");
        login.setPassword("secret123");
        ForgotPasswordRequestDTO forgot = new ForgotPasswordRequestDTO();
        forgot.setEmail("user@example.com");
        ResetPasswordRequestDTO reset = new ResetPasswordRequestDTO();
        reset.setToken("token");
        reset.setNewPassword("secret123");

        when(service.register(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(user);
        when(service.login("user@example.com", "secret123")).thenReturn(auth);
        when(authentication.getName()).thenReturn("user@example.com");
        when(service.getUserByEmail("user@example.com")).thenReturn(user);
        when(service.getAllUsers()).thenReturn(List.of(user));
        when(service.getUserIdsByRole("ADMIN")).thenReturn(List.of(1L));
        when(service.updateProfile(1L, user)).thenReturn(user);
        when(service.updateRole(1L, "MANAGER")).thenReturn(user);

        assertThat(authResource.register(register).getStatusCode().value()).isEqualTo(201);
        assertThat(authResource.login(login).getBody()).isSameAs(auth);
        assertThat(authResource.forgotPassword(forgot).getBody()).containsKey("message");
        assertThat(authResource.resetPassword(reset).getBody()).containsKey("message");
        assertThat(authResource.getCurrentUser(authentication).getBody().getEmail()).isEqualTo("user@example.com");
        assertThat(authResource.getAllUsers().getBody()).hasSize(1);
        assertThat(authResource.getIdsByRole("ADMIN").getBody()).containsExactly(1L);
        assertThat(authResource.updateProfile(1L, user).getBody()).isSameAs(user);
        assertThat(authResource.changePassword(1L, Map.of("password", "next123")).getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(authResource.deactivateUser(1L).getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(authResource.updateRole(1L, Map.of("role", "MANAGER")).getBody()).isSameAs(user);
        assertThat(authResource.deleteUser(1L).getStatusCode().value()).isEqualTo(204);

        verify(service).requestPasswordReset("user@example.com");
        verify(service).resetPassword("token", "secret123");
        verify(service).changePassword(1L, "next123");
        verify(service).deactivateUser(1L);
        verify(service).deleteUser(1L);
    }
}
