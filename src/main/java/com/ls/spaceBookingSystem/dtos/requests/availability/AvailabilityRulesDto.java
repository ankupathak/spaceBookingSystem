package com.ls.spaceBookingSystem.dtos.requests.availability;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityRulesDto {
    @Size(min = 0, max = 7)
    private List<@Valid AvailabilityRuleDto> rules;
}
