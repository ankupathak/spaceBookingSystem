package com.ls.spaceBookingSystem.dtos.requests;

import com.ls.spaceBookingSystem.common.validations.annotation.booking.ValidBookingWindow;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@ValidBookingWindow
public record BookingRequestDto(

        @NotNull Long spaceId,

        @NotNull LocalDateTime requestedStart,

        @NotNull LocalDateTime requestedEnd
) {}
