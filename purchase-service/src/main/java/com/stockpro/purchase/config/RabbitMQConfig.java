package com.stockpro.purchase.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ---- Names (keep these consistent across all services) ----
    public static final String EXCHANGE           = "stockpro.exchange";
    public static final String GOODS_RECEIVED_KEY = "goods.received";

    @Bean
    public TopicExchange stockproExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // JSON message converter — converts Java objects to JSON automatically
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
