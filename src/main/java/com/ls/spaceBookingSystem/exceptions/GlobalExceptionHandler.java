package com.ls.spaceBookingSystem.exceptions;

import com.ls.spaceBookingSystem.dtos.responses.ErrorResponse;
import com.ls.spaceBookingSystem.dtos.responses.ApiResponse;
import com.ls.spaceBookingSystem.errors.ErrorCode;
import com.ls.spaceBookingSystem.errors.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* -----------------------------------
       MethodArgumentNotValidException @Valid
       ----------------------------------- */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleAtValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> FieldError.builder()
                        .field(err.getField())
                        .message(err.getDefaultMessage())
                        .build())
                .toList();

        log.warn("[VAL_001] @Valid failed | path={} fields={}",
                request.getRequestURI(),
                fieldErrors.stream().map(FieldError::getField).toList());

        return ResponseEntity.badRequest().body(
                buildResponse(HttpStatus.BAD_REQUEST, "VAL_001", "Validation failed",
                        request, fieldErrors)
        );
    }


    /* -----------------------------------
       AppException
       ----------------------------------- */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleApp(
            AppException ex, HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();
        log.warn("[{}] {} | path={} dev={}",
                code.getCode(),
                code.getMessage(),
                request.getRequestURI(),
                ex.getDevMessage() != null ? ex.getDevMessage() : "-");
        return ResponseEntity.status(code.getStatus()).body(
                buildResponse(code.getStatus(), code.getCode(), ex.getMessage(),
                        request, toResponseFieldErrors(ex.getErrors()))
        );
    }

    /* -----------------------------------
       Helpers
       ----------------------------------- */
    private ErrorResponse buildResponse(HttpStatus status, String errorCode, String message,
                                        HttpServletRequest request,
                                        List<FieldError> errors) {
        return ErrorResponse.builder()
                .status(status.value())
                .errorCode(errorCode)
                .message(message)
                .traceId(MDC.get("traceId"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }

    private List<FieldError> toResponseFieldErrors(List<FieldError> appErrors) {
        if (appErrors == null || appErrors.isEmpty()) return null;
        return appErrors.stream()
                .map(e -> FieldError.builder()
                        .field(e.getField())
                        .message(e.getMessage())
                        .build())
                .toList();
    }
}
