package com.stockpro.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Locale;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey key;

    // ✅ Build the key once at startup
    @PostConstruct
    public void init() {
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

    // ✅ Generate Token
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)                         
                .claim("role", normalizeRole(role))
                .issuedAt(new Date())                    
                .expiration(new Date(System.currentTimeMillis() + expiration)) 
                .signWith(key)                          
                .compact();
    }

    // ✅ Validate Token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()                                
                    .verifyWith(key)                     
                    .build()
                    .parseSignedClaims(token);           
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ✅ Extract Email
    public String extractEmail(String token) {
        return Jwts.parser()                            
                .verifyWith(key)                         
                .build()
                .parseSignedClaims(token)                
                .getPayload()                            
                .getSubject();
    }

    // ✅ Extract Role
    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        return normalizedRole.startsWith("ROLE_") ? normalizedRole.substring(5) : normalizedRole;
    }
}
