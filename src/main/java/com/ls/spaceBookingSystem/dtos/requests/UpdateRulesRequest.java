package com.ls.spaceBookingSystem.dtos.requests;

import com.ls.spaceBookingSystem.dtos.requests.availability.AvailabilityRuleDto;
import com.ls.spaceBookingSystem.dtos.requests.availability.AvailabilityRulesDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class UpdateRulesRequest {
    @Valid
    private AvailabilityRulesDto rules;
}
