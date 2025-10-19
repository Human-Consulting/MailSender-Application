package com.humanconsulting.emailsender;

import com.humanconsulting.emailsender.dto.EmailCadastroRequestDto;
import com.humanconsulting.emailsender.dto.EmailUpdateRequestDto;
import com.humanconsulting.emailsender.dto.UsuarioEnviarCodigoRequestDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private final EmailNotifier emailNotifier;

    public EmailConsumer(EmailNotifier emailNotifier) {
        this.emailNotifier = emailNotifier;
    }

    @RabbitListener(queues = "email_cadastro_queue")
    public void consumeCadastro(EmailCadastroRequestDto dto) {
        System.out.println("📥 Mensagem recebida para cadastro: " + dto.getEmail());
        emailNotifier.cadastro(dto);
    }

    @RabbitListener(queues = "email_update_queue")
    public void consumeUpdate(EmailUpdateRequestDto dto) {
        System.out.println("📥 Mensagem recebida para update: " + dto.getEmailResponsavelProjeto());
        emailNotifier.update(dto);
    }

    @RabbitListener(queues = "email_codigo_queue")
    public void consumeCodigo(UsuarioEnviarCodigoRequestDto dto) {
        System.out.println("📥 Mensagem recebida para envio de código: " + dto.getEmail());
        emailNotifier.codigo(dto);
    }
}