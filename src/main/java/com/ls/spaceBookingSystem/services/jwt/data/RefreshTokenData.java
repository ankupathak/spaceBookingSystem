package com.ls.spaceBookingSystem.services.jwt.data;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class RefreshTokenData {
    private String deviceId;
    private Long userId;
    private Instant validAfter;
    private Instant issuedAt;
    private Instant expiry;
}
