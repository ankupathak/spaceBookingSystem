package com.ls.spaceBookingSystem.services;

import com.ls.spaceBookingSystem.services.email.EmailTemplate;
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

    public String render(EmailTemplate template) {
        Context context = new Context();
        template.populateContext(context);          // ← runtime dispatch
        String html = templateEngine.process(
                template.getTemplateName(), context  // ← runtime dispatch
        );
        log.info("Rendered template: {}", template.getTemplateName());
        return html;
    }
}