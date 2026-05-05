package com.ls.spaceBookingSystem.mapper;

import com.ls.spaceBookingSystem.database.entity.AvailabilityRule;
import com.ls.spaceBookingSystem.database.entity.Booking;
import com.ls.spaceBookingSystem.database.entity.Space;
import com.ls.spaceBookingSystem.database.entity.TimeSlotRange;
import com.ls.spaceBookingSystem.dtos.responses.BookingResponseDto;
import com.ls.spaceBookingSystem.dtos.responses.availability.AvailabilityRuleResponseDto;
import com.ls.spaceBookingSystem.services.TimezoneService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class AvailabilityRuleMapper {
    @Autowired
    private TimezoneService timezoneService;


    @Mapping(target = "slots", source = "slots")
    @Mapping(target = "dayOfWeek", source = "id.dayOfWeek")
    public abstract AvailabilityRuleResponseDto toResponse(AvailabilityRule rule);

    public abstract List<AvailabilityRuleResponseDto> toResponseList(List<AvailabilityRule> rules);

    /**
     * Maps Space entity → nested SpaceDto.
     * Called automatically by MapStruct when mapping the space field.
     */
    @Mapping(target = "start", source = "start")
    @Mapping(target = "end", source = "end")
    public abstract AvailabilityRuleResponseDto.TimeSlotRange toSlotDto(TimeSlotRange ruleRange);
}
