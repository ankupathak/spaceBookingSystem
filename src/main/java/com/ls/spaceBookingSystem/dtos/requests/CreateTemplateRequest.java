package com.ls.spaceBookingSystem.dtos.requests;

import com.ls.spaceBookingSystem.dtos.requests.availability.AvailabilityRuleDto;
import com.ls.spaceBookingSystem.dtos.requests.availability.AvailabilityRulesDto;
import com.ls.spaceBookingSystem.dtos.requests.availability.TemplateMetadataDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTemplateRequest extends TemplateMetadataDto {
    @Valid
    private AvailabilityRulesDto rules;
}
