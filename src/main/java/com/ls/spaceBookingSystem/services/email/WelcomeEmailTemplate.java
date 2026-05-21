package com.ls.spaceBookingSystem.services.email;

import org.thymeleaf.context.Context;

public class WelcomeEmailTemplate implements EmailTemplate {
    private final String to;
    private final String name;

    public WelcomeEmailTemplate(String to, String name) {
        this.to = to;
        this.name = name;
    }

    @Override
    public  String getTo() {
        return to;
    }

    @Override
    public  String getSubject() {
        return "\uD83D\uDC4B Welcome to Space Booking";
    }

    @Override
    public String getTemplateName() {
        return "email/new-user-welcome";
    }

    @Override
    public void populateContext(Context context) {
        context.setVariable("name", name);
    }
}
