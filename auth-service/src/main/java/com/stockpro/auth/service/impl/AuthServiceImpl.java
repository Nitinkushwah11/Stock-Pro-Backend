package com.stockpro.auth.service.impl;

import com.stockpro.auth.entity.PasswordResetToken;
import com.stockpro.auth.entity.User;
import com.stockpro.auth.dto.AuthResponseDTO;
import com.stockpro.auth.exception.UserAlreadyExistsException;
import com.stockpro.auth.repository.PasswordResetTokenRepository;
import com.stockpro.auth.repository.UserRepository;
import com.stockpro.auth.service.AuthService;
import com.stockpro.auth.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.password-reset-url}")
    private String passwordResetUrl;

    @Value("${app.password-reset.expiration-minutes:15}")
    private long passwordResetExpirationMinutes;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Override
    public User register(User user) {
        log.info("Registering user with email {}", user.getEmail());
    	// 1. MUST check for duplicate email before saving!
        if (repo.existsByEmail(user.getEmail())) {
            log.warn("Registration rejected because email already exists: {}", user.getEmail());
            throw new UserAlreadyExistsException("A user with the email " + user.getEmail() + " is already registered.");
        }

        // 2. Proceed with encoding and saving
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        // Only set default role if not provided (allows Admin to specify roles)
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("STAFF");
        } else {
            user.setRole(normalizeRole(user.getRole()));
        }
        
        // Only set default department if not provided
        if (user.getDepartment() == null || user.getDepartment().trim().isEmpty()) {
            user.setDepartment("General");
        }
        
        user.setCreatedAt(LocalDateTime.now());
        User saved = repo.save(user);
        log.info("User registered successfully with id {}", saved.getUserId());
        return saved;
    }

    @Override
    public AuthResponseDTO login(String email, String password) {
        log.info("Login attempt for email {}", email);
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Login failed due to invalid credentials for email {}", email);
            throw new RuntimeException("Invalid credentials");
        }

        user.setLastLoginAt(LocalDateTime.now());
        repo.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        log.info("Login successful for user id {}", user.getUserId());
        return AuthResponseDTO.builder()
                .token(token)
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Override
    public void logout(String token) {}

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public String refreshToken(String token) {
        return jwtUtil.generateToken(jwtUtil.extractEmail(token), "USER");
    }

    @Override
    public User getUserById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    @Override
    public User getUserByEmail(String email) {
        return repo.findByEmail(email).orElseThrow();
    }

    @Override
    public User updateProfile(Long id, User updatedUser) {
        log.info("Updating profile for user id {}", id);
        User user = getUserById(id);
        user.setFullName(updatedUser.getFullName());
        user.setPhone(updatedUser.getPhone());
        user.setDepartment(updatedUser.getDepartment());
        return repo.save(user);
    }

    @Override
    public User updateRole(Long id, String newRole) {
        log.info("Updating role for user id {}", id);
        User user = getUserById(id);
        user.setRole(normalizeRole(newRole));
        return repo.save(user);
    }

    @Override
    public void changePassword(Long id, String newPassword) {
        log.info("Changing password for user id {}", id);
        User user = getUserById(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        repo.save(user);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email {}", email);
        repo.findByEmail(email).ifPresent(user -> {
            invalidateExistingResetTokens(user);

            String rawToken = generateResetToken();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setTokenHash(hashToken(rawToken));
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(passwordResetExpirationMinutes));
            resetToken.setCreatedAt(LocalDateTime.now());
            passwordResetTokenRepository.save(resetToken);

            sendPasswordResetEmail(user, rawToken);
            log.info("Password reset email sent for user id {}", user.getUserId());
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(hashToken(token))
                .orElseThrow(() -> new RuntimeException("Invalid or expired password reset token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired password reset token");
        }

        User user = resetToken.getUser();
        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        repo.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        log.info("Password reset completed for user id {}", user.getUserId());
    }

    @Override
    public void deactivateUser(Long id) {
        log.info("Deactivating user id {}", id);
        User user = getUserById(id);
        user.setActive(false);
        repo.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user id {}", id);
        if (!repo.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        repo.deleteById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return repo.findAll();
    }

    @Override
    public List<Long> getUserIdsByRole(String role) {
        String normalizedRole = normalizeRole(role);
        List<String> acceptedRoles = new ArrayList<>();
        acceptedRoles.add(normalizedRole);
        acceptedRoles.add("ROLE_" + normalizedRole);
        return repo.findAllByRoleIn(acceptedRoles).stream()
                .filter(User::isActive)
                .map(User::getUserId)
                .toList();
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String trimmedRole = role.trim().toUpperCase(Locale.ROOT);
        return trimmedRole.startsWith("ROLE_") ? trimmedRole.substring(5) : trimmedRole;
    }

    private void invalidateExistingResetTokens(User user) {
        passwordResetTokenRepository.findByUserUserIdAndUsedFalse(user.getUserId()).forEach(token -> {
            token.setUsed(true);
            passwordResetTokenRepository.save(token);
        });
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private void sendPasswordResetEmail(User user, String rawToken) {
        String resetLink = passwordResetUrl + "?token=" + rawToken;
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailUsername != null && !mailUsername.isBlank()) {
            message.setFrom(mailUsername);
        }
        message.setTo(user.getEmail());
        message.setSubject("Reset your StockPro password");
        message.setText("""
                Hello %s,

                We received a request to reset your StockPro password.

                Open this link to set a new password:
                %s

                This link will expire in %d minutes. If you did not request this, you can ignore this email.

                StockPro Team
                """.formatted(user.getFullName(), resetLink, passwordResetExpirationMinutes));
        mailSender.send(message);
    }
}
