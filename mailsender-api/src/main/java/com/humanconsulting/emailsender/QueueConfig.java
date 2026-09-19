package com.humanconsulting.emailsender;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {
    @Bean
    public Queue resetPasswordQueue() {
        return new Queue("reset-password-queue", true);
    }
}
