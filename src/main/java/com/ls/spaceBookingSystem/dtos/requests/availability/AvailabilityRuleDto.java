package com.ls.spaceBookingSystem.dtos.requests.availability;

import com.ls.spaceBookingSystem.database.entity.DayOfWeekEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityRuleDto {
    @NotNull
    private DayOfWeekEnum dayOfWeek;

    private List<TimeRangeRequest> slots;
}
