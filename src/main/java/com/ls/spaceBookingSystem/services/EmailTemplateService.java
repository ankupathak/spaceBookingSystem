package com.ls.spaceBookingSystem.services;

import com.ls.spaceBookingSystem.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    public EmailTemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String renderCreateAccountOtpEmail(String name, String otp) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("otp", otp);
        return templateEngine.process("email/create-account-otp", context);
    }

    public String renderNewUserWelcomeEmail(String name) {
        Context context = new Context();
        context.setVariable("name", name);
        return templateEngine.process("email/new-user-welcome", context);
    }
}