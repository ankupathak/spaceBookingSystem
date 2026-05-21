package com.ls.spaceBookingSystem.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQEmailConfig {
    public static final String QUEUE = "emailQueue";
    public static final String EXCHANGE = "emailExchange";
    public static final String ROUTING_KEY = "emailRouteKey";

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-message-ttl", 300_000)
                .build();
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(emailExchange())
                .with(ROUTING_KEY);
    }
}
