package com.ls.spaceBookingSystem.dtos.responses.availability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvailabilityTemplateDto {
    private Long templateId;
    private String name;
    private int minDuration;
    private int maxDuration;
    private int bufferMinutes;
    private boolean isActive;
}
