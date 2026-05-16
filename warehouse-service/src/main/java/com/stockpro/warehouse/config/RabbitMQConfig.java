package com.stockpro.warehouse.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE              = "stockpro.exchange";
    public static final String GOODS_RECEIVED_QUEUE  = "goods.received.queue";
    public static final String GOODS_RECEIVED_KEY    = "goods.received";
    public static final String STOCK_MOVEMENT_KEY    = "stock.movement";

    @Bean
    public TopicExchange stockproExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // Declare the queue this service will CONSUME from
    @Bean
    public Queue goodsReceivedQueue() {
        return QueueBuilder.durable(GOODS_RECEIVED_QUEUE).build();
    }

    // Bind queue to exchange with routing key
    @Bean
    public Binding goodsReceivedBinding(Queue goodsReceivedQueue, TopicExchange stockproExchange) {
        return BindingBuilder.bind(goodsReceivedQueue)
                .to(stockproExchange)
                .with(GOODS_RECEIVED_KEY);
    }

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
