package com.ls.spaceBookingSystem.services;

import com.ls.spaceBookingSystem.config.JwtProperties;
import com.ls.spaceBookingSystem.errors.ErrorCode;
import com.ls.spaceBookingSystem.exceptions.AppException;
import com.ls.spaceBookingSystem.services.jwt.JwtTokenStrategy;
import com.ls.spaceBookingSystem.services.jwt.TokenValidationResult;
import com.ls.spaceBookingSystem.services.jwt.data.RefreshTokenData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
//@ConfigurationProperties(prefix = "spring.my-app.jwt")
public class JwtService {
    @Autowired
    JwtProperties jwtProperties;
    private final List<JwtTokenStrategy<?>> strategies;

    public JwtService(List<JwtTokenStrategy<?>> strategies) {
        this.strategies = strategies;
    }

    @SuppressWarnings("unchecked")
    private <T> JwtTokenStrategy<T> getStrategy(String type) {
        return (JwtTokenStrategy<T>) strategies.stream()
                .filter(s -> s.getType().equals(type))
                .findFirst()
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.UNEXPECTED).
                            withDevMessage("No strategy found for token type = "+type);
                });
    }

    public <T> String generate(String type, T data) {
        JwtTokenStrategy<T> strategy = getStrategy(type);

        JwtBuilder builder = Jwts.builder()
                .subject(strategy.resolveSubject(data))
                .claim("type", type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + strategy.getExpiryInMs()))
                .signWith(strategy.getSigningKey());

        strategy.addClaims(builder, data);

        return builder.compact();
    }

    public <T> T validateAndExtract(String token, String expectedType) {
        JwtTokenStrategy<T> strategy = getStrategy(expectedType);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(strategy.getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String actualType = claims.get("type", String.class);

            if (!expectedType.equals(actualType)) {
                throw new AppException(ErrorCode.INVALID_TOKEN_TYPE)
                        .withDevMessage("Token type mismatch expected= "+expectedType+" ,actual= "+ actualType);
            }
            Date issuedAt = claims.getIssuedAt();
            Date expiry = claims.getExpiration();
            return strategy.extractData(claims, issuedAt.toInstant(), expiry.toInstant());

        } catch (ExpiredJwtException e) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED).withDevMessage("Token Expired");
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_TOKEN)
                    .withDevMessage("Token validation failed type= "+expectedType+" error= "+e.getMessage());
        }
    }

    public boolean shouldRotate(RefreshTokenData refreshTokenData) {
        long remainingDays = ChronoUnit.DAYS.between(Instant.now(),refreshTokenData.getExpiry());
        return remainingDays < jwtProperties.getRefreshThreshHold();
    }

    public boolean isTokenExpired(String token, String type) {
        try {
            validateAndExtract(token, type);
            return false;
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.TOKEN_EXPIRED) {
                return true;
            }
            return false;
        }
    }

//    public Claims extractClaims(String token, String expectedType) {
//        JwtTokenStrategy strategy = getStrategy(expectedType);
//
//        Claims claims = Jwts.parser()
//            .verifyWith(strategy.getSigningKey())
//            .build()
//            .parseSignedClaims(token)
//            .getPayload();
//
//        String actualType = claims.get("type", String.class);
//        if (!expectedType.equals(actualType)) {
//            throw new AppException(ErrorCode.INVALID_TOKEN_TYPE);
//        }
//
//        return claims;
//    }

//    public String generateAccessToken(User user) {
//        List<String> roles = user.getRoles().stream()
//                .map(ur -> ur.getRole().getRoleName())
//                .toList();
//        return Jwts.builder()
//                .subject(user.getUserId().toString())
//                .id(UUID.randomUUID().toString())
//                .claim("email", user.getEmail())
//                .claim("type", "access")
//                .issuedAt(new Date())
//                .expiration(new Date(System.currentTimeMillis() + accessExpiry))
//                .signWith(getSigningKey())
//                .compact();
//    }
//
//    public String generateRefreshToken(User user) {
//        return Jwts.builder()
//                .subject(user.getUserId().toString())
//                .claim("type", "refresh")
//                .claim("version", user.getTokenVersion())
//                .issuedAt(new Date())
//                .expiration(new Date(System.currentTimeMillis() + refreshExpiry))
//                .signWith(getSigningKey())
//                .compact();
//    }

//    public Claims extractClaims(String token) {
//        return Jwts.parser()
//                .verifyWith(getSigningKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//    }

//    public Long extractUserId(String token) {
//        return Long.parseLong(extractClaims(token).getSubject());
//    }
//
//    public String extractJti(String token) {
//        return extractClaims(token).getId();
//    }
//
//    public int extractVersion(String token) {
//        return extractClaims(token).get("version", Integer.class);
//    }
//
//    public long getRemainingExpiry(String token) {
//        return extractClaims(token).getExpiration().getTime()
//                - System.currentTimeMillis();
//    }
//
//    public long getRemainingDays(String token) {
//        return getRemainingExpiry(token) / (1000 * 60 * 60 * 24);
//    }
//
//    public boolean shouldRotate(String token) {
//        return getRemainingDays(token) < rotateThresholdDays;
//    }
//
//    public boolean isTokenValid(String token) {
//        try {
//            extractClaims(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
    public String extractAuthTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
