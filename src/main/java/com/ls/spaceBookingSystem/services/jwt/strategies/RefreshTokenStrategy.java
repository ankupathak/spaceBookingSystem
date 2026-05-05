package com.ls.spaceBookingSystem.services.jwt.strategies;

import com.ls.spaceBookingSystem.common.config.JwtProperties;
import com.ls.spaceBookingSystem.services.jwt.JwtTokenStrategy;
import com.ls.spaceBookingSystem.services.jwt.data.RefreshTokenData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class RefreshTokenStrategy implements JwtTokenStrategy<RefreshTokenData> {

    @Autowired
    JwtProperties jwtProperties;

    @Override
    public String getType() {
        return jwtProperties.getRefreshType();
    }

    @Override
    public long getExpiryInMs() {
        return jwtProperties.getRefreshExpiryInMs();
    }

    @Override
    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getRefreshSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String resolveSubject(RefreshTokenData data) {
        return String.valueOf(data.getUserId());
    }

    @Override
    public void addClaims(JwtBuilder builder, RefreshTokenData data) {
        builder
            .claim("deviceId", data.getDeviceId())
            .claim("validAfter", Date.from(data.getValidAfter()));
    }

    @Override
    public RefreshTokenData extractData(Claims claims, Instant issuedAt, Instant expiry) {
        RefreshTokenData data = new RefreshTokenData();
        data.setUserId(Long.parseLong(claims.getSubject()));
        data.setDeviceId(claims.get("deviceId",String.class));

        Date validAfterDate = claims.get("validAfter", Date.class);
        data.setValidAfter(validAfterDate != null ? validAfterDate.toInstant() : null);

        data.setIssuedAt(issuedAt);
        data.setExpiry(expiry);
        return data;
    }
}