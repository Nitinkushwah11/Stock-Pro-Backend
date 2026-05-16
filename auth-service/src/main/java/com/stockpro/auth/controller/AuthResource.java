package com.stockpro.auth.controller;

import com.stockpro.auth.dto.LoginRequestDTO;
import com.stockpro.auth.dto.RegisterRequestDTO;
import com.stockpro.auth.dto.AuthResponseDTO;
import com.stockpro.auth.dto.ForgotPasswordRequestDTO;
import com.stockpro.auth.dto.ResetPasswordRequestDTO;
import com.stockpro.auth.entity.User;
import com.stockpro.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication API", description = "Endpoints for user registration, login, and user management")
@Slf4j
public class AuthResource {

    @Autowired
    private AuthService service;

    @PostMapping("/register")
    @Operation(summary = "Register a new user to the StockPro platform")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequestDTO dto) {
        // Map the validated DTO to the User Entity
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(dto.getPassword()); // Notice we map 'password' to 'passwordHash'
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setDepartment(dto.getDepartment());

        User savedUser = service.register(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED); // 201 Created is best practice for registration
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user and generate a JWT token")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO req) {
    	log.info("Reaching auth controller method");
        return ResponseEntity.ok(
                service.login(req.getEmail(), req.getPassword())
        );
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Send a password reset link to the user's registered email")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO req) {
        service.requestPasswordReset(req.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "If the email exists, a password reset link has been sent."
        ));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset user password using a valid email reset token")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO req) {
        service.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok(Map.of(
                "message", "Password reset successfully. Please login with your new password."
        ));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current authenticated user")
    public ResponseEntity<AuthResponseDTO> getCurrentUser(Authentication authentication) {
        User user = service.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(AuthResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build());
    }

    @GetMapping("/users")
    @Operation(summary = "Get a list of all registered users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @GetMapping("/ids-by-role/{role}")
    @Operation(summary = "Get active user IDs by role for service notifications")
    public ResponseEntity<List<Long>> getIdsByRole(@PathVariable String role) {
        return ResponseEntity.ok(service.getUserIdsByRole(role));
    }

    @PutMapping("/profile/{id}")
    @Operation(summary = "Update user profile information")
    public ResponseEntity<User> updateProfile(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(service.updateProfile(id, user));
    }

    @PutMapping("/password/{id}")
    @Operation(summary = "Change user password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        service.changePassword(id, payload.get("password"));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/deactivate/{id}")
    @Operation(summary = "Deactivate a user account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        service.deactivateUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/role/{id}")
    @Operation(summary = "Change a user's role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateRole(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        return ResponseEntity.ok(service.updateRole(id, payload.get("role")));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Permanently delete a user account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
