package com.ls.spaceBookingSystem.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.my-app.mail")
@Getter
@Setter
public class MailProperties {

    private String from;
}
