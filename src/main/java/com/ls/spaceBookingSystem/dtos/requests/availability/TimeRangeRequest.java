package com.ls.spaceBookingSystem.dtos.requests.availability;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class TimeRangeRequest {

    @NotNull
    private LocalTime start;

    @NotNull
    private LocalTime end;
}
