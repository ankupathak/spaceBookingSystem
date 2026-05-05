package com.ls.spaceBookingSystem.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class JwtProperties {

    private final String accessType = "access";

    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.access.expiry.ms}")
    private long accessExpiryInMs;

    @Value("${jwt.access.expiry.mins}")
    private int accessExpiryInMins;

    @Value("${jwt.access.expiry.days}")
    private int accessExpiryInDays;

    private final String refreshType = "refresh";

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.refresh.expiry.ms}")
    private long refreshExpiryInMs;

    @Value("${jwt.refresh.expiry.mins}")
    private int refreshExpiryInMins;

    @Value("${jwt.refresh.expiry.days}")
    private int refreshExpiryInDays;

    @Value("${jwt.refresh.rotate.threshold}")
    private int refreshThreshHold;
}
