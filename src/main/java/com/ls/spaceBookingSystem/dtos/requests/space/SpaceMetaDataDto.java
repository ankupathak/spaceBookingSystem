package com.ls.spaceBookingSystem.dtos.requests.space;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SpaceMetaDataDto {
    @NotBlank(message = "Space name is required")
    @Size(max = 30, message = "Name must not exceed 30 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull
    private long templateId;
}
