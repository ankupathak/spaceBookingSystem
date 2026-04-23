package com.ls.spaceBookingSystem.dtos.responses;

import com.ls.spaceBookingSystem.dtos.responses.availability.AvailabilityTemplateDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateTemplateResponse extends AvailabilityTemplateDto {}
