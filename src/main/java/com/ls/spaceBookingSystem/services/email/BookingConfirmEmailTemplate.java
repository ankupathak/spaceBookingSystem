package com.ls.spaceBookingSystem.services.email;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.Context;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Getter
public class BookingConfirmEmailTemplate implements EmailTemplate {

    private final String to;
    private final String name;
    private final String spaceName;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime bookingTime;

    private final String        timezone;
    private final String        bookingId;

    public BookingConfirmEmailTemplate(String to, String name, String spaceName,
                                       LocalDateTime bookingTime, String timezone,
                                       String bookingId) {
        this.to          = to;
        this.name        = name;
        this.spaceName   = spaceName;
        this.bookingTime = bookingTime;
        this.timezone    = timezone;
        this.bookingId   = bookingId;
    }

    @Override public String getTo()           { return to; }
    @Override public String getSubject()      { return "Booking Confirmed - " + spaceName; }
    @Override public String getTemplateName() { return "email/booking-confirmed"; }

    @Override
    public void populateContext(Context context) {
        context.setVariable("name",        name);
        context.setVariable("spaceName",   spaceName);
        context.setVariable("bookingTime",
                bookingTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
        context.setVariable("timezone",    timezone);
        context.setVariable("bookingId",   bookingId);
    }
}