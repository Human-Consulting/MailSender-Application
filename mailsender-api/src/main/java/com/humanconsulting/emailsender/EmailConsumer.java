package com.humanconsulting.emailsender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.humanconsulting.emailsender.dto.EmailCadastroRequestDto;
import com.humanconsulting.emailsender.dto.EmailUpdateRequestDto;
import com.humanconsulting.emailsender.dto.UsuarioEnviarCodigoRequestDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmailConsumer {
    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);

    private final EmailNotifier emailNotifier;

    public EmailConsumer(EmailNotifier emailNotifier) {
        this.emailNotifier = emailNotifier;
    }

    @RabbitListener(queues = "reset-password-queue")
    public void consumeResetPassword(Map<String, String> message) {
        String email = message.get("email");
        String token = message.get("token");
        String frontendUrl = message.get("frontendUrl");
        String resetLink = frontendUrl + "?token=" + token;
        log.info("[EMAIL] Mensagem recebida para reset de senha: {}", email);
        log.info("[EMAIL] Token recebido: {}", token);
        log.info("[EMAIL] Link de reset: {}", resetLink);
        String body = "<div style='font-family: system-ui, sans-serif, Arial; font-size: 14px; color: #333; padding: 20px 14px; background-color: #f5f5f5;'>"
                + "<div style='max-width: 600px; margin: auto; background-color: #fff;'>"
                + "<div style='text-align: center; background-color: #333; padding: 14px;'>"
                + "<a style='text-decoration: none; outline: none; color: white;' href='https://www.humanconsulting.com.br/' target='_blank'>HUMAN CONSULTING</a>"
                + "</div>"
                + "<div style='padding: 24px;'>"
                + "<h2 style='font-size: 20px; margin-bottom: 20px; text-align: center;'>Redefinição de Senha</h2>"
                + "<p style='margin-bottom: 20px; text-align: center;'>Clique no botão abaixo para redefinir sua senha:</p>"
                + "<div style='text-align: center; margin: 24px 0;'>"
                + "<a href='" + resetLink
                + "' style='background: #333; color: #fff; padding: 12px 24px; border-radius: 6px; text-decoration: none; font-size: 16px;'>Redefinir Senha</a>"
                + "</div>"
                + "<p style='text-align: center; margin-top: 20px;'>Se você não solicitou a redefinição, ignore este e-mail.</p>"
                + "<p style='margin-top: 30px; text-align: center;'>Atenciosamente,<br />Equipe Human Consulting</p>"
                + "</div>"
                + "<div style='max-width: 600px; margin: auto; text-align: center; font-size: 12px; color: #999; margin-top: 20px;'>Este email foi enviado para "
                + email + "<br />Você o recebeu porque está registrado na Human Consulting.</div>"
                + "</div>"
                + "</div>";
        try {
            emailNotifier.send(email, "Recuperação de Senha", body);
            log.info("[EMAIL] Email de reset enviado para {}", email);
        } catch (Exception e) {
            log.error("[EMAIL] Falha ao enviar email de reset para {}", email, e);
        }
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

    // Removido fluxo de código para reset de senha. Apenas JWT com URL será
    // enviado.
}