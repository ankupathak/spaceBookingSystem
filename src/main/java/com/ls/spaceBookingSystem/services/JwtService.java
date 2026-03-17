package com.ls.spaceBookingSystem.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ls.spaceBookingSystem.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
//@ConfigurationProperties(prefix = "spring.my-app.jwt")
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiry}")
    private long accessExpiry;

    @Value("${jwt.refresh.expiry}")
    private long refreshExpiry;

    @Value("${jwt.refresh.rotate.threshold}")
    private long rotateThresholdDays;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(ur -> ur.getRole().getRoleName())
                .toList();
        return Jwts.builder()
                .subject(user.getUserId().toString())
                .id(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getUserId().toString())
                .claim("type", "refresh")
                .claim("version", user.getTokenVersion())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(String token) {
        return Long.parseLong(extractClaims(token).getSubject());
    }

    public String extractJti(String token) {
        return extractClaims(token).getId();
    }

    public int extractVersion(String token) {
        return extractClaims(token).get("version", Integer.class);
    }

    public long getRemainingExpiry(String token) {
        return extractClaims(token).getExpiration().getTime()
                - System.currentTimeMillis();
    }

    public long getRemainingDays(String token) {
        return getRemainingExpiry(token) / (1000 * 60 * 60 * 24);
    }

    public boolean shouldRotate(String token) {
        return getRemainingDays(token) < rotateThresholdDays;
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
