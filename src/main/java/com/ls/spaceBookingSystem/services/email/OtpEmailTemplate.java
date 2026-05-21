package com.ls.spaceBookingSystem.services.email;

import org.thymeleaf.context.Context;

public class OtpEmailTemplate implements EmailTemplate {
    private final String to;
    private final String name;
    private final String otp;

    public OtpEmailTemplate(String to, String name, String otp) {
        this.to = to;
        this.name = name;
        this.otp = otp;
    }

    @Override
    public  String getTo() {
        return to;
    }

    @Override
    public  String getSubject() {
        return "Otp for Account Creation for Space Booking System";
    }

    @Override
    public String getTemplateName() {
        return "email/create-account-otp";
    }

    @Override
    public void populateContext(Context context) {
        context.setVariable("name", name);
        context.setVariable("otp", otp);
    }


}
