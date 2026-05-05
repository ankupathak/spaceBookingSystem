package com.ls.spaceBookingSystem.common.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    UNAUTHENTICATED ("AUTH_001", "Authentication required", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED   ("AUTH_002", "Access denied",           HttpStatus.FORBIDDEN),
    ACCESS_REVOKED   ("AUTH_003", "Account access revoked",           HttpStatus.UNAUTHORIZED),
    CONCURRENT_REFRESH   ("AUTH_004", "",       HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS   ("AUTH_005", "Invalid email or password",       HttpStatus.UNAUTHORIZED),

    TOKEN_EXPIRED   ("TOKEN_001", "Token has expired",       HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_TYPE   ("TOKEN_002", "Token type mismatch",       HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN   ("TOKEN_003", "Invalid Token",       HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED   ("TOKEN_004", "Token revoked",       HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_EXPIRED   ("TOKEN_005", "Token not yet expired",       HttpStatus.UNAUTHORIZED),

    NOT_FOUND        ("USER_001", "User not found",                       HttpStatus.NOT_FOUND),
    EMAIL_TAKEN      ("USER_002", "Email is already registered",          HttpStatus.CONFLICT),
    EMAIL_NOT_VERIFIED      ("USER_003", "Create an account inorder to login!",          HttpStatus.CONFLICT),
    ACCOUNT_INACTIVE ("USER_004", "Account is inactive",                  HttpStatus.FORBIDDEN),

    OTP_NOT_FOUND        ("OTP_001", "OTP not found or already used",         HttpStatus.NOT_FOUND),
    OTP_EXPIRED          ("OTP_002", "OTP has expired",                       HttpStatus.GONE),
    OTP_INVALID          ("OTP_003", "OTP is incorrect",                      HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPTS     ("OTP_004", "Too many incorrect attempts",           HttpStatus.TOO_MANY_REQUESTS),
    OTP_ALREADY_VERIFIED ("OTP_005", "This OTP has already been verified",    HttpStatus.CONFLICT),
    OTP_SEND_FAILED      ("OTP_006", "Failed to send OTP. Please try again",  HttpStatus.INTERNAL_SERVER_ERROR),
    OTP_RESEND_TOO_SOON  ("OTP_007", "Please wait before requesting a new OTP", HttpStatus.TOO_MANY_REQUESTS),

    UNEXPECTED          ("SYS_001", "An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_FAILED  ("SYS_002", "File upload failed. Please try again.",                HttpStatus.INTERNAL_SERVER_ERROR),
    GATEWAY_FAILED      ("SYS_003", "External service is currently unavailable.",           HttpStatus.SERVICE_UNAVAILABLE),
    DATABASE_ERROR      ("SYS_004", "A database error occurred. Please try again.",         HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_REQUEST ("VAL_001", "Validation failed", HttpStatus.BAD_REQUEST),

    SPACE_NOT_FOUND      ("SPACE_001", "Space not found",   HttpStatus.BAD_REQUEST),
    SPACE_NOT_AVAILABILE      ("SPACE_002", "Space is not available",   HttpStatus.BAD_REQUEST),

    AVAILABILITY      ("AV_001", "",   HttpStatus.BAD_REQUEST),
    INVALID_TIME_RANGE      ("AV_002", "You have overlapping time",   HttpStatus.BAD_REQUEST),
    SLOT_DURATION_MISMATCH      ("AV_003", "",   HttpStatus.BAD_REQUEST),
    TEMPLATE_NOT_FOUND      ("AV_004", "Template not found",   HttpStatus.BAD_REQUEST),

    BOOKING      ("BK_001", "",   HttpStatus.BAD_REQUEST),
    BOOKING_MIN_MAX_DURATION_MISMATCH      ("BK_002", "Booking duration is outside the allowed range.",   HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND      ("BK_003", "Booking not found",   HttpStatus.BAD_REQUEST),
    BOOKING_SLOT_TAKEN      ("BK_004", "Booking Slot already taken",   HttpStatus.BAD_REQUEST),
    BOOKING_RACE_CONDITION      ("BK_005", "Someone else trying to booking the same slot!",   HttpStatus.CONFLICT),

    TEST      ("TEST_001", "Testing Error.",         HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
