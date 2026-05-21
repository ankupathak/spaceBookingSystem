package com.ls.spaceBookingSystem.messageBroker.rabbitMq.Producers;

import com.ls.spaceBookingSystem.common.config.RabbitMQEmailConfig;
import com.ls.spaceBookingSystem.services.email.EmailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailProducer {

    private final AmqpTemplate amqpTemplate;

    public EmailProducer(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void publishEmailEvent(EmailTemplate template) {
        log.info("Publishing email event: {}", template);
        amqpTemplate.convertAndSend(
                RabbitMQEmailConfig.EXCHANGE,
                RabbitMQEmailConfig.ROUTING_KEY,
                template
        );
        log.info("Email event published successfully for template={}", template.getTemplateName());
    }

}
