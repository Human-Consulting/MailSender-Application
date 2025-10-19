package com.humanconsulting.emailsender;

import com.humanconsulting.emailsender.dto.EmailCadastroRequestDto;
import com.humanconsulting.emailsender.dto.EmailUpdateRequestDto;
import com.humanconsulting.emailsender.dto.UsuarioEnviarCodigoRequestDto;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class EmailNotifier {
    private final JavaMailSender emailSender;

    public EmailNotifier(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void update(EmailUpdateRequestDto dto) {
        List<String> emails = new ArrayList<>();
        emails.add(dto.getEmailResponsavelProjeto());
        if (!dto.getEmailResponsavelProjeto().equals(dto.getEmailResponsavelTarefa()))
            emails.add(dto.getEmailResponsavelTarefa());
        String assunto = "Impedimento no Projeto: " + dto.getDescricaoProjeto();
        String mensagem = String.format("""
                            <div style='font-family: system-ui, sans-serif, Arial; font-size: 14px; color: #333; padding: 20px 14px; background-color: #f5f5f5;'>
                              <div style='max-width: 600px; margin: auto; background-color: #fff;'>
                                <div style='text-align: center; background-color: #333; padding: 14px;'>
                                  <a style='text-decoration: none; outline: none; color: white;' href='https://www.humanconsulting.com.br/' target='_blank'>
                                    HUMAN CONSULTING
                                  </a>
                                </div>
                                <div style='padding: 24px;'>
                                  <h2 style='font-size: 20px; margin-bottom: 20px; text-align: center;'>%s</h2>
                                  <p><strong>Projeto:</strong> %s</p>
                                  <p><strong>Sprint:</strong> %s</p>
                                  <p><strong>Tarefa:</strong> %s</p>
                                  <p><strong>Responsável:</strong> %s</p>
                                  <p><strong>Comentário:</strong> %s</p>
                                  <p style='margin-top: 30px; text-align: center;'>Atenciosamente,<br />Equipe Human Consulting</p>
                                </div>
                              </div>
                              <div style='max-width: 600px; margin: auto; text-align: center; font-size: 12px; color: #999; margin-top: 20px;'>
                                Este email foi enviado para %s<br />
                                Você o recebeu porque está registrado na Human Consulting.
                              </div>
                            </div>
                        """,
                dto.isComImpedimento() ? "Impedimento resolvido!" : "Novo impedimento identificado!",
                dto.getDescricaoProjeto(),
                dto.getDescricaoSprint(),
                dto.getDescricaoTarefa(),
                dto.getNomeResponsavelTarefa(),
                dto.getComentario(),
                String.join(", ", emails)
        );
        enviarEmail(emails, assunto, mensagem);
    }

    public void cadastro(EmailCadastroRequestDto dto) {
        List<String> email = Collections.singletonList(dto.getEmail());
        String assunto = "Bem vindo à Human Consulting";
        String conteudo = String.format("""
                            <div style='font-family: system-ui, sans-serif, Arial; font-size: 14px; color: #333; padding: 20px 14px; background-color: #f5f5f5;'>
                              <div style='max-width: 600px; margin: auto; background-color: #fff;'>
                                <div style='text-align: center; background-color: #333; padding: 14px;'>
                                  <a style='text-decoration: none; outline: none; color: white;' href='https://www.humanconsulting.com.br/' target='_blank'>
                                    HUMAN CONSULTING
                                  </a>
                                </div>
                                <div style='padding: 24px;'>
                                  <h1 style='font-size: 22px; margin-bottom: 20px; text-align: center;'>
                                    Bem-vindo(a), %s!
                                  </h1>
                                  <p style='margin-bottom: 20px; text-align: center;'>
                                    Seu usuário foi adicionado aos projetos da <strong>%s</strong> como <strong>%s</strong> na área de <strong>%s</strong>.
                                  </p>
                                  <p style='margin-bottom: 20px; text-align: center;'>
                                    Use as credenciais abaixo para seu primeiro acesso:
                                  </p>
                                  <div style='text-align: center; margin: 24px 0;'>
                                    <div style='font-size: 16px; margin-bottom: 8px;'><strong>Email:</strong> %s</div>
                                    <div style='font-size: 16px;'><strong>Senha:</strong> %s</div>
                                  </div>
                                  <p style='text-align: center; margin-top: 20px;'>
                                    Por segurança, recomendamos que altere sua senha após o primeiro login.
                                  </p>
                                  <p style='margin-top: 30px; text-align: center;'>
                                    Atenciosamente,<br />
                                    Equipe Human Consulting
                                  </p>
                                </div>
                              </div>
                              <div style='max-width: 600px; margin: auto; text-align: center; font-size: 12px; color: #999; margin-top: 20px;'>
                                Este email foi enviado para %s<br />
                                Você o recebeu porque está registrado na Human Consulting.
                              </div>
                            </div>
                        """,
                dto.getNome(),
                dto.getNomeEmpresa(),
                dto.getCargo(),
                dto.getArea(),
                dto.getEmail(),
                dto.getSenha(),
                dto.getEmail()
        );
        enviarEmail(email, assunto, conteudo);
    }

    public void codigo(UsuarioEnviarCodigoRequestDto dto) {
        List<String> email = new ArrayList<>();
        email.add(dto.getEmail());
        String conteudo = String.format("""
                            <div
                                    style="
                                      font-family: system-ui, sans-serif, Arial;
                                      font-size: 14px;
                                      color: #333;
                                      padding: 20px 14px;
                                      background-color: #f5f5f5;
                                    "
                                  >
                                    <div style="max-width: 600px; margin: auto; background-color: #fff">
                                      <div style="text-align: center; background-color: #333; padding: 14px">
                                        <a style='text-decoration: none; outline: none; color: white;' href='https://www.humanconsulting.com.br/' target='_blank'>
                                            HUMAN CONSULTING
                                        </a>
                                      </div>
                                      <div style="padding: 24px">
                                        <h1 style="font-size: 22px; margin-bottom: 20px; text-align: center;">
                                          Código de Verificação
                                        </h1>
                                        <p style="margin-bottom: 20px; text-align: center;">
                                          Utilize o código abaixo para validar seu acesso ou redefinir sua senha:
                                        </p>
                        
                                        <!-- Quadradinhos do código -->
                                        <div style="text-align: center; margin: 24px 0;">
                                          <span style="
                                            display: inline-block;
                                            width: 42px;
                                            height: 42px;
                                            margin: 0 4px;
                                            border: 2px solid #333;
                                            border-radius: 6px;
                                            line-height: 42px;
                                            font-size: 20px;
                                            font-weight: bold;
                                          ">
                                            %s
                                          </span>
                                          <span style="display: inline-block; width: 42px; height: 42px; margin: 0 4px; border: 2px solid #333; border-radius: 6px; line-height: 42px; font-size: 20px; font-weight: bold;">
                                            %s
                                          </span>
                                          <span style="display: inline-block; width: 42px; height: 42px; margin: 0 4px; border: 2px solid #333; border-radius: 6px; line-height: 42px; font-size: 20px; font-weight: bold;">
                                            %s
                                          </span>
                                          <span style="display: inline-block; width: 42px; height: 42px; margin: 0 4px; border: 2px solid #333; border-radius: 6px; line-height: 42px; font-size: 20px; font-weight: bold;">
                                            %s
                                          </span>
                                          <span style="display: inline-block; width: 42px; height: 42px; margin: 0 4px; border: 2px solid #333; border-radius: 6px; line-height: 42px; font-size: 20px; font-weight: bold;">
                                            %s
                                          </span>
                                          <span style="display: inline-block; width: 42px; height: 42px; margin: 0 4px; border: 2px solid #333; border-radius: 6px; line-height: 42px; font-size: 20px; font-weight: bold;">
                                            %s
                                          </span>
                                        </div>
                        
                                        <p style="text-align: center;">
                                          Se você não solicitou este código, ignore este email.
                                        </p>
                        
                                        <p style="margin-top: 30px; text-align: center;">
                                          Atenciosamente,<br />
                                          Equipe Human Consulting
                                        </p>
                                      </div>
                                    </div>
                                    <div style="max-width: 600px; margin: auto; text-align: center; font-size: 12px; color: #999; margin-top: 20px;">
                                      Este email foi enviado para %s<br />
                                      Você o recebeu porque está registrado na Human Consulting
                                    </div>
                                  </div>
                        """,
                dto.getCodigo().charAt(0),
                dto.getCodigo().charAt(1),
                dto.getCodigo().charAt(2),
                dto.getCodigo().charAt(3),
                dto.getCodigo().charAt(4),
                dto.getCodigo().charAt(5),
                dto.getEmail()
        );
        enviarEmail(email, "Reset de Senha", conteudo);
    }

    private void enviarEmail(List<String> destinatarios, String assunto, String conteudo) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(new InternetAddress("contato@humanconsulting.com.br", "Human Consulting"));
            helper.setTo(destinatarios.toArray(new String[0]));
            helper.setSubject(assunto);
            helper.setText(conteudo, true);
            emailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar e-mail", e);
        }
    }
}
