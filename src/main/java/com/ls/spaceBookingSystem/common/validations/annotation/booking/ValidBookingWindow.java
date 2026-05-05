package com.ls.spaceBookingSystem.common.validations.annotation.booking;

import com.ls.spaceBookingSystem.common.validations.validators.booking.BookingWindowValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BookingWindowValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBookingWindow {
    String message() default "Invalid booking window";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
