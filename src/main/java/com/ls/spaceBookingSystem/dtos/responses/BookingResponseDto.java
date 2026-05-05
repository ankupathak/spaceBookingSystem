package com.ls.spaceBookingSystem.dtos.responses;

import com.ls.spaceBookingSystem.database.entity.Booking;
import com.ls.spaceBookingSystem.common.enums.BookingStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Builder
public record BookingResponseDto(
        Long          bookingId,
        SpaceDto      space,
        ZonedDateTime requestedStart,
        ZonedDateTime requestedEnd,
        boolean       multiDay,
        BookingStatus status,
        ZonedDateTime createdAt,
        ZonedDateTime cancelledAt
) {
    // nested record inside the outer record
    public record SpaceDto(
            String name,
            String description
    ) {}
}
