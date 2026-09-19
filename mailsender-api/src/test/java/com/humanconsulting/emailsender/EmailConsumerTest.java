package com.humanconsulting.emailsender;

import com.humanconsulting.emailsender.dto.EmailCadastroRequestDto;
import com.humanconsulting.emailsender.dto.EmailUpdateRequestDto;
import com.humanconsulting.emailsender.dto.UsuarioEnviarCodigoRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Regression tests for the infinite-redelivery-loop fix.
 *
 * EmailConsumer wraps every emailNotifier call in a try/catch and, on failure,
 * rethrows an AmqpRejectAndDontRequeueException. Spring AMQP's listener container
 * treats that exception type as "reject, do not requeue" -- which is what stops a
 * malformed/permanently-failing message (e.g. an invalid verification code) from
 * being redelivered forever. Before this fix, the raw exception was allowed to
 * propagate, and with the default container settings that causes the broker to
 * requeue and redeliver the same message indefinitely.
 */
@ExtendWith(MockitoExtension.class)
class EmailConsumerTest {

    @Mock
    private EmailNotifier emailNotifier;

    // ---------------- cadastro ----------------

    @Test
    void consumeCadastro_delegatesToNotifier() {
        EmailConsumer consumer = new EmailConsumer(emailNotifier);
        EmailCadastroRequestDto dto = new EmailCadastroRequestDto();
        dto.setEmail("user@example.com");

        assertThatCode(() -> consumer.consumeCadastro(dto)).doesNotThrowAnyException();

        verify(emailNotifier).cadastro(dto);
    }

    @Test
    void consumeCadastro_notifierFailure_isWrappedAsRejectAndDontRequeue() {
        EmailConsumer consumer = new EmailConsumer(emailNotifier);
        EmailCadastroRequestDto dto = new EmailCadastroRequestDto();
        dto.setEmail("user@example.com");
        RuntimeException cause = new RuntimeException("boom");
        doThrow(cause).when(emailNotifier).cadastro(any());

        assertThatThrownBy(() -> consumer.consumeCadastro(dto))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasCause(cause);
    }

    // ---------------- update ----------------

    @Test
    void consumeUpdate_delegatesToNotifier() {
        EmailConsumer consumer = new EmailConsumer(emailNotifier);
        EmailUpdateRequestDto dto = new EmailUpdateRequestDto();
        dto.setEmailResponsavelProjeto("ana@example.com");

        assertThatCode(() -> consumer.consumeUpdate(dto)).doesNotThrowAnyException();

        verify(emailNotifier).update(dto);
    }

    @Test
    void consumeUpdate_notifierFailure_isWrappedAsRejectAndDontRequeue() {
        EmailConsumer consumer = new EmailConsumer(emailNotifier);
        EmailUpdateRequestDto dto = new EmailUpdateRequestDto();
        dto.setEmailResponsavelProjeto("ana@example.com");
        RuntimeException cause = new RuntimeException("boom");
        doThrow(cause).when(emailNotifier).update(any());

        assertThatThrownBy(() -> consumer.consumeUpdate(dto))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasCause(cause);
    }

    // ---------------- codigo ----------------

    @Test
    void consumeCodigo_delegatesToNotifier() {
        EmailConsumer consumer = new EmailConsumer(emailNotifier);
        UsuarioEnviarCodigoRequestDto dto = new UsuarioEnviarCodigoRequestDto();
        dto.setEmail("user@example.com");
        dto.setCodigo("123456");

        assertThatCode(() -> consumer.consumeCodigo(dto)).doesNotThrowAnyException();

        verify(emailNotifier).codigo(dto);
    }

    /**
     * The core regression scenario: a malformed message (null/invalid codigo) makes
     * EmailNotifier#codigo throw IllegalArgumentException. Before the fix this would
     * propagate straight out of the @RabbitListener method and the broker would
     * requeue + redeliver it forever. Now it must come out as
     * AmqpRejectAndDontRequeueException so the container discards/DLQs it instead.
     */
    @Test
    void consumeCodigo_malformedDto_doesNotCauseInfiniteRequeue() {
        EmailConsumer consumer = new EmailConsumer(emailNotifier);
        UsuarioEnviarCodigoRequestDto dto = new UsuarioEnviarCodigoRequestDto();
        dto.setEmail("user@example.com");
        dto.setCodigo(null);
        IllegalArgumentException cause = new IllegalArgumentException("Código de verificação inválido");
        doThrow(cause).when(emailNotifier).codigo(any());

        assertThatThrownBy(() -> consumer.consumeCodigo(dto))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasCause(cause);
    }

    @Test
    void consumeCodigo_notifierFailure_isWrappedAsRejectAndDontRequeue() {
        EmailConsumer consumer = new EmailConsumer(emailNotifier);
        UsuarioEnviarCodigoRequestDto dto = new UsuarioEnviarCodigoRequestDto();
        dto.setEmail("user@example.com");
        dto.setCodigo("123456");
        RuntimeException cause = new RuntimeException("smtp down");
        doThrow(cause).when(emailNotifier).codigo(any());

        assertThatThrownBy(() -> consumer.consumeCodigo(dto))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasCause(cause);
    }
}
