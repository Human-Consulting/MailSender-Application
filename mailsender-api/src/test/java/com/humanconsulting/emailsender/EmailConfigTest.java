package com.humanconsulting.emailsender;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for the dead-letter-exchange/queue wiring added this session to stop
 * malformed messages from being redelivered forever. These are plain unit tests against
 * the @Bean-producing methods directly (no Spring context, no real broker needed) --
 * they just assert the DLQ topology is declared with the expected names/routing keys.
 */
class EmailConfigTest {

    private final EmailConfig config = new EmailConfig();

    @Test
    void deadLetterExchange_hasExpectedName() {
        assertThat(config.emailDeadLetterExchange().getName()).isEqualTo("email.dlx");
    }

    @Test
    void cadastroQueue_isDurableAndRoutesToDlq() {
        Queue queue = config.emailCadastroQueue();
        assertThat(queue.getName()).isEqualTo("email_cadastro_queue");
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments())
                .containsEntry("x-dead-letter-exchange", "email.dlx")
                .containsEntry("x-dead-letter-routing-key", "email_cadastro_queue.dlq");
    }

    @Test
    void cadastroDeadLetterQueue_hasExpectedName() {
        assertThat(config.emailCadastroDeadLetterQueue().getName()).isEqualTo("email_cadastro_queue.dlq");
    }

    @Test
    void cadastroDeadLetterBinding_bindsQueueToExchangeWithRoutingKey() {
        Binding binding = config.emailCadastroDeadLetterBinding();
        assertThat(binding.getDestination()).isEqualTo("email_cadastro_queue.dlq");
        assertThat(binding.getExchange()).isEqualTo("email.dlx");
        assertThat(binding.getRoutingKey()).isEqualTo("email_cadastro_queue.dlq");
    }

    @Test
    void updateQueue_isDurableAndRoutesToDlq() {
        Queue queue = config.emailUpdateQueue();
        assertThat(queue.getName()).isEqualTo("email_update_queue");
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments())
                .containsEntry("x-dead-letter-exchange", "email.dlx")
                .containsEntry("x-dead-letter-routing-key", "email_update_queue.dlq");
    }

    @Test
    void updateDeadLetterQueue_hasExpectedName() {
        assertThat(config.emailUpdateDeadLetterQueue().getName()).isEqualTo("email_update_queue.dlq");
    }

    @Test
    void updateDeadLetterBinding_bindsQueueToExchangeWithRoutingKey() {
        Binding binding = config.emailUpdateDeadLetterBinding();
        assertThat(binding.getDestination()).isEqualTo("email_update_queue.dlq");
        assertThat(binding.getExchange()).isEqualTo("email.dlx");
        assertThat(binding.getRoutingKey()).isEqualTo("email_update_queue.dlq");
    }

    @Test
    void codigoQueue_isDurableAndRoutesToDlq() {
        Queue queue = config.emailCodigoQueue();
        assertThat(queue.getName()).isEqualTo("email_codigo_queue");
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments())
                .containsEntry("x-dead-letter-exchange", "email.dlx")
                .containsEntry("x-dead-letter-routing-key", "email_codigo_queue.dlq");
    }

    @Test
    void codigoDeadLetterQueue_hasExpectedName() {
        assertThat(config.emailCodigoDeadLetterQueue().getName()).isEqualTo("email_codigo_queue.dlq");
    }

    @Test
    void codigoDeadLetterBinding_bindsQueueToExchangeWithRoutingKey() {
        Binding binding = config.emailCodigoDeadLetterBinding();
        assertThat(binding.getDestination()).isEqualTo("email_codigo_queue.dlq");
        assertThat(binding.getExchange()).isEqualTo("email.dlx");
        assertThat(binding.getRoutingKey()).isEqualTo("email_codigo_queue.dlq");
    }
}
