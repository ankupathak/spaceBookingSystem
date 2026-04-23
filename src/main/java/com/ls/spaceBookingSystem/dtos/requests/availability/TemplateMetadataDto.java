package com.ls.spaceBookingSystem.dtos.requests.availability;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TemplateMetadataDto {

    @NotBlank
    private String name;

    @Min(30)
    @Max(1440)
    private int minDuration;

    @Min(30)
    @Max(1440)
    private int maxDuration;

    @NotNull
    @Min(0)
    @Max(1440)
    private int bufferMinutes;
}
