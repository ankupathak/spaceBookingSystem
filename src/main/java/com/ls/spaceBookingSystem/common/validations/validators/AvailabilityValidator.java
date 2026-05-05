package com.ls.spaceBookingSystem.common.validations.validators;

import com.ls.spaceBookingSystem.dtos.requests.availability.AvailabilityRuleDto;
import com.ls.spaceBookingSystem.dtos.requests.availability.TimeRangeRequest;
import com.ls.spaceBookingSystem.database.entity.DayOfWeekEnum;
import com.ls.spaceBookingSystem.common.errors.ErrorCode;
import com.ls.spaceBookingSystem.common.exceptions.AppException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AvailabilityValidator {

    public void validateMinMaxDuration(int min, int max) {

        if (min > max) {
            throw new AppException(ErrorCode.AVAILABILITY)
                    .withErrors("min","Min duration is greater than max duration");
        }
    }

    public void validateRules(List<AvailabilityRuleDto> rules) {

        Set<DayOfWeekEnum> seen = new HashSet<>();

        for (AvailabilityRuleDto rule : rules) {
            DayOfWeekEnum dayOfWeek = rule.getDayOfWeek();
            if(seen.contains(dayOfWeek)) throw new AppException(ErrorCode.AVAILABILITY, "Duplicate week days");
            seen.add(dayOfWeek);

            List<TimeRangeRequest> slots = rule.getSlots();

            slots.sort(Comparator.comparing(TimeRangeRequest::getStart));

            validateSlots(slots);
        }
    }

    public static void validateSlots(List<TimeRangeRequest> slots) {

        slots.sort(Comparator.comparing(TimeRangeRequest::getStart));

        for (int i = 0; i < slots.size(); i++) {

            TimeRangeRequest curr = slots.get(i);

            if (!curr.getStart().isBefore(curr.getEnd())) {
                throw new AppException(ErrorCode.INVALID_TIME_RANGE);
            }

            if (i > 0) {
                TimeRangeRequest prev = slots.get(i - 1);

                if (!curr.getStart().isAfter(prev.getEnd())) {
                    throw new AppException(ErrorCode.INVALID_TIME_RANGE);
                }
            }
        }
    }
}
