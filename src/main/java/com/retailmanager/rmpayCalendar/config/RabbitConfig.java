package com.retailmanager.rmpayCalendar.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration // TODO:DESCOMENTAR SI SE USAN COLA
public class RabbitConfig {

    public static final String QUEUE = "shopify.product.queue";
    public static final String EXCHANGE = "shopify.exchange";
    public static final String ROUTING_KEY = "product.sync";

    //@Bean // TODO:DESCOMENTAR SI SE USAN COLA
    public Queue queue() {
        return new Queue(QUEUE, true);
    }

    //@Bean // TODO:DESCOMENTAR SI SE USAN COLA
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    //@Bean // TODO:DESCOMENTAR SI SE USAN COLA
    public Binding binding() {
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(ROUTING_KEY);
    }
   //@Bean // TODO:DESCOMENTAR SI SE USAN COLA
public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
}
}