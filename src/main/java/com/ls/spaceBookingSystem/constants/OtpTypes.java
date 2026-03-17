package com.ls.spaceBookingSystem.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OtpTypes {
    REGISTRATION_EMAIL_OTP ("create.account.email.otp", 1);

    private final String code;
    private final int id;
}

