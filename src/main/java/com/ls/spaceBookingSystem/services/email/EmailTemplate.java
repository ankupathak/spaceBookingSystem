package com.ls.spaceBookingSystem.services.email;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.thymeleaf.context.Context;

public interface EmailTemplate {

    String getSubject();
    String getTo();
    String getTemplateName();
    void populateContext(Context context);

    @JsonIgnore
    default String getFrom() {
        return "no-reply@spacebooking.com";
    }
}
