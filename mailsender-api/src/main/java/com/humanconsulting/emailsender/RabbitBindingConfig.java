package com.humanconsulting.emailsender;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitBindingConfig {
    @Bean
    public DirectExchange resetPasswordExchange() {
        return new DirectExchange("emailsender.direct.exchange");
    }

    @Bean
    public Binding resetPasswordBinding(Queue resetPasswordQueue, DirectExchange resetPasswordExchange) {
        return BindingBuilder.bind(resetPasswordQueue)
                .to(resetPasswordExchange)
                .with("email.reset-senha");
    }
}
