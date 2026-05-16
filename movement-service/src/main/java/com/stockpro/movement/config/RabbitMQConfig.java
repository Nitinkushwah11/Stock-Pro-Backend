package com.stockpro.movement.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE             = "stockpro.exchange";
    public static final String STOCK_MOVEMENT_QUEUE = "stock.movement.queue";
    public static final String STOCK_MOVEMENT_KEY   = "stock.movement";

    @Bean
    public TopicExchange stockproExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue stockMovementQueue() {
        return QueueBuilder.durable(STOCK_MOVEMENT_QUEUE).build();
    }

    @Bean
    public Binding stockMovementBinding(Queue stockMovementQueue, TopicExchange stockproExchange) {
        return BindingBuilder.bind(stockMovementQueue)
                .to(stockproExchange)
                .with(STOCK_MOVEMENT_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
