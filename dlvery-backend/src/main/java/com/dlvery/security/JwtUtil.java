package com.dlvery.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger; // ADDED: For debugging
import org.slf4j.LoggerFactory; // ADDED: For debugging
import jakarta.annotation.PostConstruct; // ADDED: For initialization check

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    // ADDED: Logger to debug issues
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // ADDED: Ensure secret is initialized
    @PostConstruct
    public void init() {
        if (secret == null || secret.isEmpty()) {
            logger.error("JWT secret is not configured properly");
            throw new IllegalStateException("JWT secret must be configured in application.properties");
        }
        logger.info("JWT secret loaded successfully");
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username, String role) {
        // ADDED: Log to ensure secret is loaded
        logger.debug("Generating token for username: {}, role: {}, secret: {}", username, role, secret != null ? "loaded" : "null");
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // ADDED: Log validation errors
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}