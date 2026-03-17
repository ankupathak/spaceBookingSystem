package com.ls.spaceBookingSystem.exceptions;

import com.ls.spaceBookingSystem.errors.ErrorCode;
import com.ls.spaceBookingSystem.errors.FieldError;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
@Setter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private String devMessage;
    private List<FieldError> errors;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AppException withDevMessage(String devMessage) {
        this.devMessage = devMessage;
        return this;
    }

    public AppException withErrors(List<FieldError> errors) {
        this.errors = errors;
        return this;
    }
}
