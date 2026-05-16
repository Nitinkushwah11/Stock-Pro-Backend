package com.stockpro.auth.service.impl;

import com.stockpro.auth.dto.AuthResponseDTO;
import com.stockpro.auth.entity.PasswordResetToken;
import com.stockpro.auth.entity.User;
import com.stockpro.auth.repository.PasswordResetTokenRepository;
import com.stockpro.auth.repository.UserRepository;
import com.stockpro.auth.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository repo;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginReturnsTokenAndUserDetailsWhenCredentialsAreValid() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("user@example.com");
        user.setFullName("Test User");
        user.setRole("ADMIN");
        user.setPasswordHash("encoded-password");

        when(repo.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken("user@example.com", "ADMIN")).thenReturn("jwt-token");

        AuthResponseDTO response = authService.login("user@example.com", "plain-password");

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        verify(repo).save(user);
    }

    @Test
    void registerAppliesDefaultsAndEncodesPassword() {
        User user = user();
        user.setRole(null);
        user.setDepartment(null);
        when(repo.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(repo.save(user)).thenReturn(user);

        User saved = authService.register(user);

        assertThat(saved.getRole()).isEqualTo("STAFF");
        assertThat(saved.getDepartment()).isEqualTo("General");
        assertThat(saved.getPasswordHash()).isEqualTo("encoded-password");
    }

    @Test
    void profileRolePasswordAndLookupMethodsUseRepository() {
        User user = user();
        User update = new User();
        update.setFullName("Updated User");
        update.setPhone("9999999999");
        update.setDepartment("Ops");

        when(repo.findById(1L)).thenReturn(Optional.of(user));
        when(repo.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(repo.save(user)).thenReturn(user);
        when(passwordEncoder.encode("next-password")).thenReturn("next-encoded");
        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.extractEmail("token")).thenReturn("user@example.com");
        when(jwtUtil.generateToken("user@example.com", "USER")).thenReturn("new-token");
        when(repo.findAll()).thenReturn(List.of(user));
        when(repo.findAllByRoleIn(List.of("ADMIN", "ROLE_ADMIN"))).thenReturn(List.of(user));
        when(repo.existsById(1L)).thenReturn(true);

        assertThat(authService.getUserById(1L)).isSameAs(user);
        assertThat(authService.getUserByEmail("user@example.com")).isSameAs(user);
        assertThat(authService.updateProfile(1L, update).getFullName()).isEqualTo("Updated User");
        assertThat(authService.updateRole(1L, "MANAGER").getRole()).isEqualTo("MANAGER");
        authService.changePassword(1L, "next-password");
        assertThat(user.getPasswordHash()).isEqualTo("next-encoded");
        assertThat(authService.validateToken("token")).isTrue();
        assertThat(authService.refreshToken("token")).isEqualTo("new-token");
        assertThat(authService.getAllUsers()).hasSize(1);
        assertThat(authService.getUserIdsByRole("ROLE_ADMIN")).containsExactly(1L);
        authService.deactivateUser(1L);
        assertThat(user.isActive()).isFalse();
        authService.deleteUser(1L);
        verify(repo).deleteById(1L);
    }

    @Test
    void resetPasswordUpdatesUserAndToken() {
        User user = user();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(passwordResetTokenRepository.findByTokenHash(any(String.class))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("new-password")).thenReturn("new-encoded");

        authService.resetPassword("raw-token", "new-password");

        assertThat(user.getPasswordHash()).isEqualTo("new-encoded");
        assertThat(token.isUsed()).isTrue();
        verify(repo).save(user);
        verify(passwordResetTokenRepository).save(token);
    }

    @Test
    void requestPasswordResetInvalidatesOldTokensAndSendsEmail() {
        User user = user();
        PasswordResetToken oldToken = new PasswordResetToken();
        oldToken.setUser(user);

        ReflectionTestUtils.setField(authService, "passwordResetUrl", "http://localhost/reset");
        ReflectionTestUtils.setField(authService, "passwordResetExpirationMinutes", 15L);
        ReflectionTestUtils.setField(authService, "mailUsername", "noreply@stockpro.local");
        when(repo.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findByUserUserIdAndUsedFalse(1L)).thenReturn(List.of(oldToken));

        authService.requestPasswordReset("user@example.com");

        assertThat(oldToken.isUsed()).isTrue();
        verify(passwordResetTokenRepository).save(oldToken);
        verify(passwordResetTokenRepository, times(2)).save(any(PasswordResetToken.class));
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    private User user() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("user@example.com");
        user.setFullName("Test User");
        user.setRole("ADMIN");
        user.setDepartment("IT");
        user.setPhone("9876543210");
        user.setPasswordHash("plain-password");
        user.setActive(true);
        return user;
    }
}
