package com.ls.spaceBookingSystem.services.jwt.data;

import lombok.Data;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
public class AccessTokenData {
    private Long userId;
    private String deviceId;
    private List<String> roles;
    private Instant issuedAt;
    private Instant expiry;
}
