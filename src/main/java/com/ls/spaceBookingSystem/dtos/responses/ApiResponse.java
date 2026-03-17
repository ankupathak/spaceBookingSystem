package com.ls.spaceBookingSystem.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private Map<String, Object> metadata;
    private String message;
    private Integer errorCode;
    private Object errors;
    private LocalDateTime timestamp;

    /* -------------------------
       SUCCESS RESPONSES
       ------------------------- */

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> success(T data, Map<String, Object> metadata) {
        ApiResponse<T> response = success(data);
        response.metadata = metadata;
        return response;
    }

    /* -------------------------
       GLOBAL ERROR
       ------------------------- */

    public static ApiResponse<?> error(int errorCode, String message) {
        ApiResponse<?> response = new ApiResponse<>();
        response.success = false;
        response.errorCode = errorCode;
        response.message = message;
        return response;
    }

    public static ApiResponse<?> error(
            int errorCode,
            String message,
            Map<String, Object> metadata) {

        ApiResponse<?> response = error(errorCode, message);
        response.metadata = metadata;
        return response;
    }

    /* -------------------------
       FIELD / STRUCTURED ERRORS
       ------------------------- */

    public static ApiResponse<?> validationError(
            int errorCode,
            Map<String, Object> errors) {

        ApiResponse<?> response = new ApiResponse<>();
        response.success = false;
        response.errorCode = errorCode;
        response.errors = errors;
        return response;
    }

    public static ApiResponse<?> validationError(
            int errorCode,
            String message,
            Map<String, Object> errors) {

        ApiResponse<?> response = validationError(errorCode, errors);
        response.message = message;
        return response;
    }

    /* -------------------------
       GENERIC ERROR (no code)
       ------------------------- */

    public static ApiResponse<?> error(Map<String, Object> errors) {
        ApiResponse<?> response = new ApiResponse<>();
        response.success = false;
        response.errors = errors;
        return response;
    }
}
