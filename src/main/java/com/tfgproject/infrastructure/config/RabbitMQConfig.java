package com.tfgproject.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // === NOMBRES DE COLAS ===
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String SMS_QUEUE = "sms.queue";
    public static final String SCHEDULED_QUEUE = "scheduled.queue";

    // === EXCHANGES ===
    public static final String DIRECT_EXCHANGE = "messaging.direct";
    public static final String FANOUT_EXCHANGE = "messaging.fanout";

    // === ROUTING KEYS ===
    public static final String EMAIL_ROUTING_KEY = "message.email";
    public static final String SMS_ROUTING_KEY = "message.sms";

    // === CREAR COLAS ===
    @Bean
    public Queue emailQueue() {
        return QueueBuilder
                .durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "dlx.email")
                .build();
    }

    @Bean
    public Queue smsQueue() {
        return QueueBuilder
                .durable(SMS_QUEUE)
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "dlx.sms")
                .build();
    }

    @Bean
    public Queue scheduledQueue() {
        return QueueBuilder
                .durable(SCHEDULED_QUEUE)
                .build();
    }

    // === CREAR EXCHANGES ===
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    // === BINDINGS PARA ENVÍO ÚNICO ===
    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(directExchange())
                .with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding smsBinding() {
        return BindingBuilder
                .bind(smsQueue())
                .to(directExchange())
                .with(SMS_ROUTING_KEY);
    }

    // === BINDINGS PARA DIFUSIÓN (FANOUT) ===
    @Bean
    public Binding emailFanoutBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(fanoutExchange());
    }

    @Bean
    public Binding smsFanoutBinding() {
        return BindingBuilder
                .bind(smsQueue())
                .to(fanoutExchange());
    }

    // === CONFIGURAR RABBIT TEMPLATE ===
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }

    // === CONVERTER PARA JSON ===
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}