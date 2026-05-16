package com.stockpro.auth.config;

import com.stockpro.auth.entity.User;
import com.stockpro.auth.repository.UserRepository;
import com.stockpro.auth.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final String frontendSuccessUrl;

    public OAuth2LoginSuccessHandler(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     JwtUtil jwtUtil,
                                     @Value("${app.frontend.oauth-success-url}") String frontendSuccessUrl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.frontendSuccessUrl = frontendSuccessUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String fullName = oauthUser.getAttribute("name");

        if (email == null || email.isBlank()) {
            response.sendRedirect(frontendSuccessUrl + "?error=missing_email");
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createOAuthUser(email, fullName));
        user.setLastLoginAt(LocalDateTime.now());
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendSuccessUrl)
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private User createOAuthUser(String email, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName != null && !fullName.isBlank() ? fullName : email);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole("STAFF");
        user.setDepartment("Google OAuth");
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        return user;
    }
}
