package com.humanconsulting.emailsender;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    private static final String DEAD_LETTER_EXCHANGE = "email.dlx";

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        return factory;
    }

    @Bean
    public DirectExchange emailDeadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    // ---- email_cadastro_queue ----

    @Bean
    public Queue emailCadastroQueue() {
        return QueueBuilder.durable("email_cadastro_queue")
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "email_cadastro_queue.dlq")
                .build();
    }

    @Bean
    public Queue emailCadastroDeadLetterQueue() {
        return QueueBuilder.durable("email_cadastro_queue.dlq").build();
    }

    @Bean
    public Binding emailCadastroDeadLetterBinding() {
        return BindingBuilder.bind(emailCadastroDeadLetterQueue())
                .to(emailDeadLetterExchange())
                .with("email_cadastro_queue.dlq");
    }

    // ---- email_update_queue ----

    @Bean
    public Queue emailUpdateQueue() {
        return QueueBuilder.durable("email_update_queue")
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "email_update_queue.dlq")
                .build();
    }

    @Bean
    public Queue emailUpdateDeadLetterQueue() {
        return QueueBuilder.durable("email_update_queue.dlq").build();
    }

    @Bean
    public Binding emailUpdateDeadLetterBinding() {
        return BindingBuilder.bind(emailUpdateDeadLetterQueue())
                .to(emailDeadLetterExchange())
                .with("email_update_queue.dlq");
    }

    // ---- email_codigo_queue ----

    @Bean
    public Queue emailCodigoQueue() {
        return QueueBuilder.durable("email_codigo_queue")
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "email_codigo_queue.dlq")
                .build();
    }

    @Bean
    public Queue emailCodigoDeadLetterQueue() {
        return QueueBuilder.durable("email_codigo_queue.dlq").build();
    }

    @Bean
    public Binding emailCodigoDeadLetterBinding() {
        return BindingBuilder.bind(emailCodigoDeadLetterQueue())
                .to(emailDeadLetterExchange())
                .with("email_codigo_queue.dlq");
    }
}
