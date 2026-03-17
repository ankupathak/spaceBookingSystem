package com.ls.spaceBookingSystem.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    UNAUTHENTICATED ("AUTH_001", "Authentication required", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED   ("AUTH_002", "Access denied",           HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED   ("AUTH_003", "Token has expired",       HttpStatus.UNAUTHORIZED),
    CONCURRENT_REFRESH   ("AUTH_004", "",       HttpStatus.UNAUTHORIZED),

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
    DATABASE_ERROR      ("SYS_004", "A database error occurred. Please try again.",         HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
