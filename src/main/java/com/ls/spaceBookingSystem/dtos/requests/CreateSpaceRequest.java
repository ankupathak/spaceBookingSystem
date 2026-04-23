package com.ls.spaceBookingSystem.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateSpaceRequest {
    @NotBlank(message = "Space name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;
}
