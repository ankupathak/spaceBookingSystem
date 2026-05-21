package com.ls.spaceBookingSystem.messageBroker.rabbitMq.Consumers;

import com.ls.spaceBookingSystem.common.config.RabbitMQEmailConfig;
import com.ls.spaceBookingSystem.services.EmailService;
import com.ls.spaceBookingSystem.services.EmailTemplateService;
import com.ls.spaceBookingSystem.services.email.EmailTemplate;
import com.ls.spaceBookingSystem.services.email.OtpEmailTemplate;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class EmailConsumer {

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = RabbitMQEmailConfig.QUEUE)
    public void process(EmailTemplate template, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("Received email request: template={}: subject={}", template.getTemplateName(), template.getSubject());
        try {
            sendOtp(template);
            channel.basicAck(deliveryTag, false);

        } catch (Exception ex) {
            log.error("Exception, Failed to send Email Exception={}", ex.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void sendOtp(EmailTemplate template) {
        String html = emailTemplateService.render(template);
        emailService.sendMail(template.getTo(),template.getSubject(), html);
    }
}
