package com.ls.spaceBookingSystem.dtos.requests.availability;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeRangeRequest {

    @NotNull
    private LocalTime start;

    @NotNull
    private LocalTime end;
}
