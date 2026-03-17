package com.ls.spaceBookingSystem.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    // Store only jti, not full token
    public void blacklistJti(String jti, long expiryMillis) {
        if (expiryMillis > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + jti,
                    "1",
                    expiryMillis,
                    TimeUnit.MILLISECONDS   // auto-deleted by Redis
            );
        }
    }

    public boolean isJtiBlacklisted(String jti) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + jti);
//        return Boolean.TRUE.equals(
//                redisTemplate.hasKey(BLACKLIST_PREFIX + jti)
//        );
    }

    // Emergency — blacklist all tokens for a user (account compromise)
    public void blacklistAllUserTokens(Long userId, long expiryMillis) {
        redisTemplate.opsForValue().set(
                "revoked_user:" + userId,
                String.valueOf(System.currentTimeMillis()),
                expiryMillis,
                TimeUnit.MILLISECONDS
        );
    }

    public boolean isUserEmergencyRevoked(Long userId, Date tokenIssuedAt) {
        String revokedAt = redisTemplate.opsForValue().get("revoked_user:" + userId);
        if (revokedAt == null) return false;
        return tokenIssuedAt.getTime() < Long.parseLong(revokedAt);
    }
}
