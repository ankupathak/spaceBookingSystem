package com.ls.spaceBookingSystem.common.validations.validators.booking;

import com.ls.spaceBookingSystem.common.validations.annotation.booking.ValidBookingWindow;
import com.ls.spaceBookingSystem.dtos.requests.BookingRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BookingWindowValidator
        implements ConstraintValidator<ValidBookingWindow, BookingRequestDto> {

    @Override
    public boolean isValid(BookingRequestDto dto, ConstraintValidatorContext context) {
        if (dto.requestedStart() == null || dto.requestedEnd() == null) {
            return true; // let @NotNull handle it
        }

        return dto.requestedStart().isBefore(dto.requestedEnd());
    }
}
