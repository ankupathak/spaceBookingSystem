package com.ls.spaceBookingSystem.services.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenValidationResult<T> {
    private final T data;   // ← typed, no casting needed by callers
}
