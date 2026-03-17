package com.ls.spaceBookingSystem.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ls.spaceBookingSystem.errors.FieldError;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String errorCode;
    private String message;
    private String traceId;
    private String path;
    private LocalDateTime timestamp;
    private List<FieldError> errors;
}
