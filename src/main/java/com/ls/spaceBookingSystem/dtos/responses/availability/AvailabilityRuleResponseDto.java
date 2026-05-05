package com.ls.spaceBookingSystem.dtos.responses.availability;

import com.ls.spaceBookingSystem.common.enums.BookingStatus;
import com.ls.spaceBookingSystem.database.entity.DayOfWeekEnum;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record AvailabilityRuleResponseDto(
        DayOfWeekEnum dayOfWeek,
        boolean isFullDay,
        List<TimeSlotRange> slots
) {
    public record  TimeSlotRange(
            LocalTime start,
            LocalTime end
    ){}
}

