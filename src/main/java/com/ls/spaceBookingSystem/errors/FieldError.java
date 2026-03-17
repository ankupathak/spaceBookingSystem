package com.ls.spaceBookingSystem.errors;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FieldError {
    private final String field;
    private final String message;

    public static FieldError of(String field, String message) {
        return FieldError.builder()
                .field(field)
                .message(message)
                .build();
    }
}
