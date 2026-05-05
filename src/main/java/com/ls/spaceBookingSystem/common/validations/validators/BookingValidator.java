package com.ls.spaceBookingSystem.common.validations.validators;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ls.spaceBookingSystem.common.config.AppConfig;
import com.ls.spaceBookingSystem.common.errors.ErrorCode;
import com.ls.spaceBookingSystem.common.exceptions.AppException;
import com.ls.spaceBookingSystem.database.entity.AvailabilityRule;
import com.ls.spaceBookingSystem.database.entity.AvailabilityTemplate;
import com.ls.spaceBookingSystem.database.entity.DayOfWeekEnum;
import com.ls.spaceBookingSystem.database.entity.TimeSlotRange;
import com.ls.spaceBookingSystem.services.TimezoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingValidator {


    private final TimezoneService timezoneService;
    private final AppConfig appConfig;

    public void validateBookingRequest(AvailabilityTemplate template,
                                       ZoneId ownerZone,
                                       Instant start,
                                       Instant end,
                                       ZoneId  userZone,
                                       long durationMinutes
    ) {

        validateMinMaxDuration(template.getMinBookingMinutes(),template.getMaxBookingMinutes(),durationMinutes);

        Map<DayOfWeekEnum, AvailabilityRule> ruleMap = template.getRules().stream()
            .collect(Collectors.toMap(AvailabilityRule::getDayOfWeek, r -> r));

        ZonedDateTime ownerZonedStart = timezoneService.toUserTime(start,ownerZone);
        ZonedDateTime ownerZonedEnd   = timezoneService.toUserTime(end,ownerZone);

        LocalDate startDate = ownerZonedStart.toLocalDate();
        LocalDate endDate   = ownerZonedEnd.toLocalDate();


        if (startDate.equals(endDate)) {
            validateSingleDay(ruleMap, ownerZonedStart, ownerZonedEnd, ownerZone);
        } else {
            validateMultiDay(ruleMap, ownerZonedStart, ownerZonedEnd, startDate, endDate, ownerZone);
        }
    }

    public void validateMinMaxDuration(int minBookingMinute, int maxBookingMinutes, long requestedMinutes) {

        if (requestedMinutes < minBookingMinute) {
            throw new AppException(
                    ErrorCode.BOOKING_MIN_MAX_DURATION_MISMATCH,
                    "Minimum booking duration is " + minBookingMinute
                            + " minutes. Requested: " + requestedMinutes
            );
        }
        if (maxBookingMinutes > 0 && requestedMinutes > maxBookingMinutes) {
            throw new AppException(
                    ErrorCode.BOOKING_MIN_MAX_DURATION_MISMATCH,
                    "Maximum booking duration is " + maxBookingMinutes
                            + " minutes. Requested: " + requestedMinutes
            );
        }
    }

    private void validateSingleDay(Map<DayOfWeekEnum, AvailabilityRule> ruleMap,
                                   ZonedDateTime start,
                                   ZonedDateTime end,
                                   ZoneId ownerZone) {
        AvailabilityRule rule = getRule(ruleMap, start);

        LocalTime s = start.toLocalTime();
        LocalTime e = end.toLocalTime();

        boolean covered = rule.getSlots().stream().anyMatch(slot -> {
            LocalTime ss = LocalTime.parse(slot.start(), appConfig.getBookingTimeFMT());
            LocalTime se = LocalTime.parse(slot.end(),   appConfig.getBookingTimeFMT());
            return !s.isBefore(ss) && !e.isAfter(se);
        });

        if (!covered) throw new AppException(ErrorCode.SPACE_NOT_AVAILABILE)
                .withDevMessage("Requested window " + s + "–" + e
                        + " (in space owner's timezone " + ownerZone + ")"
                        + " is outside available slots on " + dayOf(start));
    }

    // ── Multi-day ────────────────────────────────────────────────────────

    private void validateMultiDay(Map<DayOfWeekEnum, AvailabilityRule> ruleMap,
                                  ZonedDateTime start,
                                  ZonedDateTime end,
                                  LocalDate startDate,
                                  LocalDate endDate,
                                  ZoneId ownerZone) {
        // Day 1 — start must fall within an open slot
        validateDayStart(ruleMap, start, ownerZone);

        // Middle days — a rule must exist (space must be open that day)
        for (LocalDate d = startDate.plusDays(1); d.isBefore(endDate); d = d.plusDays(1)) {
            DayOfWeekEnum day = DayOfWeekEnum.valueOf(d.getDayOfWeek().name());
            AvailabilityRule rule = ruleMap.get(day);

            if (rule == null)
                throw new AppException(ErrorCode.SPACE_NOT_AVAILABILE)
                        .withDevMessage("Space is closed on " + day + " (" + d + ")");

            if (!rule.isFullDay())
                throw new AppException(ErrorCode.SPACE_NOT_AVAILABILE)
                        .withDevMessage("Space is not open for the full 24 hours on " + day
                                + " (" + d + "). Multi-day bookings require all "
                                + "middle days to be marked as fully open.");
        }

        // Last day — end must fall within an open slot
        validateDayEnd(ruleMap, end, ownerZone);
    }

    private void validateDayStart(Map<DayOfWeekEnum, AvailabilityRule> ruleMap,
                                  ZonedDateTime start,
                                  ZoneId ownerZone) {
        AvailabilityRule rule = getRule(ruleMap, start);
        LocalTime t = start.toLocalTime();

        boolean ok = rule.getSlots().stream().anyMatch(s -> {
            LocalTime ss = LocalTime.parse(s.start(), appConfig.getBookingTimeFMT());
            LocalTime se = LocalTime.parse(s.end(),   appConfig.getBookingTimeFMT());
            return !t.isBefore(ss) && t.isBefore(se);
        });

        if (!ok) throw new AppException(ErrorCode.SPACE_NOT_AVAILABILE)
                .withDevMessage("Start time " + t + " (in owner's timezone " + ownerZone + ")"
                        + " is outside available slots on " + dayOf(start));
    }

    private void validateDayEnd(Map<DayOfWeekEnum, AvailabilityRule> ruleMap,
                                ZonedDateTime end,
                                ZoneId ownerZone) {
        AvailabilityRule rule = getRule(ruleMap, end);
        LocalTime t = end.toLocalTime();

        boolean ok = rule.getSlots().stream().anyMatch(s -> {
            LocalTime ss = LocalTime.parse(s.start(), appConfig.getBookingTimeFMT());
            LocalTime se = LocalTime.parse(s.end(),   appConfig.getBookingTimeFMT());
            return !t.isBefore(ss) && !t.isAfter(se);
        });

        if (!ok) throw new AppException(ErrorCode.SPACE_NOT_AVAILABILE)
                .withDevMessage("End time " + t + " (in owner's timezone " + ownerZone + ")"
                        + " is outside available slots on " + dayOf(end));
    }

    private AvailabilityRule getRule(Map<DayOfWeekEnum, AvailabilityRule> ruleMap,
                                     ZonedDateTime dt) {
        DayOfWeekEnum day  = dayOf(dt);
        AvailabilityRule slots = ruleMap.get(day);
        if (slots == null)
            throw new AppException(ErrorCode.SPACE_NOT_AVAILABILE)
                    .withDevMessage("Space is not available on " + day);
        return slots;
    }

    private DayOfWeekEnum dayOf(ZonedDateTime dt) {
        return DayOfWeekEnum.valueOf(dt.getDayOfWeek().name());
    }
}
