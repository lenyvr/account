package com.devsu.fintech.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange accountsExchange() {
        return new DirectExchange("accounts.exchange");
    }

    @Bean
    public Queue checkRequestQueue() {
        return new Queue("accounts.check-request");
    }

    @Bean
    public Binding checkRequestBinding(Queue checkRequestQueue, DirectExchange accountsExchange) {
        return BindingBuilder.bind(checkRequestQueue).to(accountsExchange).with("accounts.check-request");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
