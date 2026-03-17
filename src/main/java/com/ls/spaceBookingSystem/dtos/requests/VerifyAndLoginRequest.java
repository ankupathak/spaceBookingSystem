package com.ls.spaceBookingSystem.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyAndLoginRequest {
    @NotBlank(message = "otp is required")
    private String otp;

    @Email(message = "Invalid email")
    @NotBlank
    private String email;
}

