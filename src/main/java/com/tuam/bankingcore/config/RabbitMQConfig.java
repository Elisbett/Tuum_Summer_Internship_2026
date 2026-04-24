package com.tuam.bankingcore.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ACCOUNT_EVENTS_EXCHANGE = "tuam.account.events";

    @Bean
    public TopicExchange accountEventsExchange() {
        return new TopicExchange(ACCOUNT_EVENTS_EXCHANGE);
    }
}