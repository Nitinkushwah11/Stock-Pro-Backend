package com.stockpro.purchase.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    void init() {
        this.key = buildSigningKey(secret);
    }

    private SecretKey buildSigningKey(String secret) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length >= 32) {
            return Keys.hmacShaKeyFor(secretBytes);
        }
        try {
            return Keys.hmacShaKeyFor(MessageDigest.getInstance("SHA-256").digest(secretBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                Claims claims = Jwts.parser().verifyWith(key).build()
                        .parseSignedClaims(authHeader.substring(7)).getPayload();
                String role = claims.get("role", String.class);
                String authority = role != null && role.startsWith("ROLE_") ? role : "ROLE_" + role;
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, List.of(new SimpleGrantedAuthority(authority))));
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
