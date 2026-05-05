package com.ls.spaceBookingSystem.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.my-app.mail")
@Getter
@Setter
public class MailProperties {

    @Value("${spring.myApp.email.from}")
    private String from;
}
