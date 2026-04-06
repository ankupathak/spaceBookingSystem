package com.ls.spaceBookingSystem.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "bl:";

    // Store only jti, not full token
    public void blacklist(String key, String value, long expiryInMins) {
        if (expiryInMins > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX+key,
                    "1",
                    expiryInMins,
                    TimeUnit.MINUTES   // auto-deleted by Redis
            );
        }
    }

//    public boolean containsKey(String key) {
//        return redisTemplate.hasKey(AUTH_PREFIX+key);
////        return Boolean.TRUE.equals(
////                redisTemplate.hasKey(BLACKLIST_PREFIX + jti)
////        );
//
//    }

    public boolean isTokenBlackListedOrInvalidated(String jti, long userId, Instant tokenIssueAt) {
        List<String> keys = List.of(
                BLACKLIST_PREFIX + userId + ":" + jti,
                BLACKLIST_PREFIX + userId
        );

        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        if (values == null) return false;

        String isTokenBlacklisted = values.get(0);
        String tokenInvalidatedAt = values.get(1);

        // Check 1 — individual jti blacklisted?
        // "1" means blacklisted — just presence check
        if ("1".equals(isTokenBlacklisted)) {
            return true;
        }

        // Check 2 — Token invalidated
        // timestamp means ALL tokens issued before this time are invalid
        return isTokenInvalidated(tokenInvalidatedAt,tokenIssueAt);
    }

    public String getValue(String key) {
        return redisTemplate.opsForValue().get(BLACKLIST_PREFIX+key);
    }

    public boolean containsKey(String key) {
        return redisTemplate.opsForValue().get(BLACKLIST_PREFIX+key) != null;
    }

    public boolean isTokenInvalidated(String tokenInvalidatedAt, Instant tokenIssueAt) {
        if (tokenInvalidatedAt != null) {
            Instant revokedAt = Instant.ofEpochMilli(Long.parseLong(tokenInvalidatedAt));
            return tokenIssueAt.isBefore(revokedAt);
        }

        return false;
    }

//    public boolean isUserEmergencyRevoked(Long userId, Date tokenIssuedAt) {
//        String revokedAt = redisTemplate.opsForValue().get("revoked_user:" + userId);
//        if (revokedAt == null) return false;
//        return tokenIssuedAt.getTime() < Long.parseLong(revokedAt);
//    }
}
