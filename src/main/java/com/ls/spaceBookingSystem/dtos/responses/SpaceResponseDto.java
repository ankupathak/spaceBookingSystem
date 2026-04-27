package com.ls.spaceBookingSystem.dtos.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpaceResponseDto {
    private Long    spaceId;
    private String  name;
    private String  description;
    private Long    templateId;
    private boolean active;
}
