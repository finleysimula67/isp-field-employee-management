package com.workflow.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final int MIN_SECRET_LENGTH = 32;
    private final String rawSecret;
    private final SecretKey jwtSecret;
    private final long jwtExpirationMs;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String jwtSecret,
                            @Value("${app.jwt.expiration-ms}") long jwtExpirationMs) {
        this.rawSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        if (jwtSecret == null || jwtSecret.length() < MIN_SECRET_LENGTH) {
            log.warn("JWT secret is too short (< {} chars). Hashing to derive a secure key.", MIN_SECRET_LENGTH);
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
                this.jwtSecret = Keys.hmacShaKeyFor(hash);
            } catch (Exception e) {
                throw new RuntimeException("Failed to derive JWT key", e);
            }
        } else {
            this.jwtSecret = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        }
    }

    @PostConstruct
    public void validate() {
        if (log.isDebugEnabled()) {
            log.debug("JWT secret key length: {} bytes (minimum recommended: {})", rawSecret.length(), MIN_SECRET_LENGTH);
        }
    }

    public String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email).claim("role", role)
                .issuedAt(now).expiration(expiry)
                .signWith(jwtSecret).compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(jwtSecret).build()
                .parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try { Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }
}
