package com.humanconsulting.emailsender;

import com.humanconsulting.emailsender.dto.EmailCadastroRequestDto;
import com.humanconsulting.emailsender.dto.EmailUpdateRequestDto;
import com.humanconsulting.emailsender.dto.UsuarioEnviarCodigoRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EmailConsumer.class);

    private final EmailNotifier emailNotifier;

    public EmailConsumer(EmailNotifier emailNotifier) {
        this.emailNotifier = emailNotifier;
    }

    @RabbitListener(queues = "email_cadastro_queue")
    public void consumeCadastro(EmailCadastroRequestDto dto) {
        logger.info("Mensagem recebida para cadastro: {}", dto.getEmail());
        try {
            emailNotifier.cadastro(dto);
        } catch (Exception e) {
            logger.error("Falha ao processar mensagem de cadastro para {}", dto.getEmail(), e);
            throw new AmqpRejectAndDontRequeueException("Falha ao processar mensagem de cadastro", e);
        }
    }

    @RabbitListener(queues = "email_update_queue")
    public void consumeUpdate(EmailUpdateRequestDto dto) {
        logger.info("Mensagem recebida para update: {}", dto.getEmailResponsavelProjeto());
        try {
            emailNotifier.update(dto);
        } catch (Exception e) {
            logger.error("Falha ao processar mensagem de update para {}", dto.getEmailResponsavelProjeto(), e);
            throw new AmqpRejectAndDontRequeueException("Falha ao processar mensagem de update", e);
        }
    }

    @RabbitListener(queues = "email_codigo_queue")
    public void consumeCodigo(UsuarioEnviarCodigoRequestDto dto) {
        logger.info("Mensagem recebida para envio de código: {}", dto.getEmail());
        try {
            emailNotifier.codigo(dto);
        } catch (Exception e) {
            logger.error("Falha ao processar mensagem de código para {}", dto.getEmail(), e);
            throw new AmqpRejectAndDontRequeueException("Falha ao processar mensagem de código", e);
        }
    }
}
