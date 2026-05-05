package com.ls.spaceBookingSystem.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@Getter
public class AppConfig {

    private final DateTimeFormatter bookingTimeFMT = DateTimeFormatter.ofPattern("HH:mm");
}

