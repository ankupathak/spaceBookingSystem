package com.ls.spaceBookingSystem.exceptions;

import com.ls.spaceBookingSystem.dtos.responses.ErrorResponse;
import com.ls.spaceBookingSystem.dtos.responses.ApiResponse;
import com.ls.spaceBookingSystem.errors.ErrorCode;
import com.ls.spaceBookingSystem.errors.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
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
    @Value("${spring.environment}")
    private String applicationEnviroment;

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

        ErrorResponse.ErrorResponseBuilder buildError = ErrorResponse.builder();
        buildError
            .status(HttpStatus.BAD_REQUEST.value())
            .errorCode("VAL_001")
            .message("Validation failed")
            .errors(fieldErrors);

        return ResponseEntity.badRequest().body(
                buildError.build()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {

        System.out.println("HttpMessageNotReadableException -----------------------------> ");
        ErrorCode code = ErrorCode.INVALID_REQUEST;
        ErrorResponse.ErrorResponseBuilder buildError = ErrorResponse.builder();
        buildError
                .status(code.getStatus().value())
                .errorCode(code.getCode())
                .message(ex.getMessage())
                .message("Malformed or unrecognized field");

        return ResponseEntity.badRequest().body(
                buildError.build()
        );
    }


    /* -----------------------------------
       AppException
       ----------------------------------- */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleApp(
            AppException ex, HttpServletRequest request) {
        System.out.println("AppException -----------------------------> ");
        ErrorCode code = ex.getErrorCode();
        log.warn("[{}] {} | path={} dev={}",
                code.getCode(),
                code.getMessage(),
                request.getRequestURI(),
                ex.getDevMessage() != null ? ex.getDevMessage() : "-");
        ErrorResponse.ErrorResponseBuilder buildError = ErrorResponse.builder();
        buildError
            .status(code.getStatus().value())
            .errorCode(code.getCode())
            .message(ex.getMessage())
            .errors(toResponseFieldErrors(ex.getErrors()));
        buildResponse(buildError,request);
        return ResponseEntity.status(code.getStatus()).body(
                buildError.build()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        ErrorCode invalidCredential = ErrorCode.INVALID_CREDENTIALS;
        ErrorResponse.ErrorResponseBuilder buildError = ErrorResponse.builder();
        buildError
            .status(invalidCredential.getStatus().value())
            .errorCode(invalidCredential.getCode())
            .message(invalidCredential.getMessage());


        return ResponseEntity.status(invalidCredential.getStatus()).body(
                buildError.build()
        );
    }

    /* -----------------------------------
       Helpers
       ----------------------------------- */
    private void buildResponse(ErrorResponse.ErrorResponseBuilder buildError, HttpServletRequest request) {
            buildError
                .traceId(MDC.get("traceId"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now());
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
