package com.devsu.fintech.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private static final String EXCHANGE_NAME = "accounts.exchange";
    private static final String QUEUE_NAME = "accounts.check-request";
    private static final String ROUTING_KEY = "accounts.check-request";
    private static final String TRUSTED_PACKAGES = "*";


    @Bean
    public DirectExchange accountsExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue checkRequestQueue() {
        return new Queue(QUEUE_NAME);
    }

    @Bean
    public Binding checkRequestBinding(Queue checkRequestQueue, DirectExchange accountsExchange) {
        return BindingBuilder.bind(checkRequestQueue).to(accountsExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages(TRUSTED_PACKAGES);
        converter.setJavaTypeMapper(typeMapper);

        return converter;
    }
}
