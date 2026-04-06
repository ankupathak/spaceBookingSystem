package com.ls.spaceBookingSystem.services.jwt.strategies;

import com.ls.spaceBookingSystem.config.JwtProperties;
import com.ls.spaceBookingSystem.services.jwt.JwtTokenStrategy;
import com.ls.spaceBookingSystem.services.jwt.data.AccessTokenData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class AccessTokenStrategy implements JwtTokenStrategy<AccessTokenData> {

    @Autowired
    JwtProperties jwtProperties;

    @Override
    public String getType() {
        return jwtProperties.getAccessType();
    }

    @Override
    public long getExpiryInMs() {
        return jwtProperties.getAccessExpiryInMs();
    }

    @Override
    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getAccessSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String resolveSubject(AccessTokenData data) {
        return String.valueOf(data.getUserId());
    }

    @Override
    public void addClaims(JwtBuilder builder, AccessTokenData data) {
        builder
            .claim("deviceId", data.getDeviceId())
            .claim("roles", data.getRoles());
    }

    @Override
    public AccessTokenData extractData(Claims claims, Instant issuedAt, Instant expiry) {
        AccessTokenData data = new AccessTokenData();
        data.setUserId(Long.parseLong(claims.getSubject()));
        data.setDeviceId(claims.get("deviceId", String.class));
        Object rolesObj = claims.get("roles");
        List<String> roles = (rolesObj instanceof List<?> list )
                ? list.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList() : List.of();
        data.setRoles(roles);

        data.setIssuedAt(issuedAt);
        data.setExpiry(expiry);
        return data;
    }
}