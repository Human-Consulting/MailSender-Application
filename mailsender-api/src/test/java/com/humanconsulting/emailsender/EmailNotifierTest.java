package com.humanconsulting.emailsender;

import com.humanconsulting.emailsender.dto.EmailCadastroRequestDto;
import com.humanconsulting.emailsender.dto.EmailUpdateRequestDto;
import com.humanconsulting.emailsender.dto.UsuarioEnviarCodigoRequestDto;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regression tests for EmailNotifier covering this session's fixes:
 *  - explicit UTF-8 charset on outgoing MimeMessages (accented pt-BR text must survive round-trip)
 *  - HTML-escaping of user-controlled fields interpolated into the email templates
 *  - null-safety of the recipient list built in update()
 */
@ExtendWith(MockitoExtension.class)
class EmailNotifierTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailNotifier emailNotifier;

    @BeforeEach
    void setUp() {
        emailNotifier = new EmailNotifier(mailSender);
        // EmailNotifier calls emailSender.createMimeMessage() itself; give it a REAL MimeMessage
        // (backed by a real Session) so MimeMessageHelper actually runs its normal encoding path
        // instead of hitting mock stubs. This lets us decode the real bytes it produced.
        // lenient(): the codigo()-validation tests below throw before ever reaching this call.
        lenient().when(mailSender.createMimeMessage()).thenAnswer(invocation -> newRealMimeMessage());
    }

    private static MimeMessage newRealMimeMessage() {
        return new MimeMessage(Session.getDefaultInstance(new Properties()));
    }

    /** Recursively pulls all textual content out of a (possibly multipart) MimeMessage. */
    private static String extractText(Object content) throws Exception {
        if (content instanceof String s) {
            return s;
        }
        if (content instanceof Multipart multipart) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                sb.append(extractText(part.getContent()));
            }
            return sb.toString();
        }
        return String.valueOf(content);
    }

    /** Recursively collects every Content-Type header found across a (possibly multipart) message. */
    private static String collectContentTypes(jakarta.mail.Part part) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(part.getContentType()).append(" | ");
        Object content = part.getContent();
        if (content instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                sb.append(collectContentTypes(multipart.getBodyPart(i)));
            }
        }
        return sb.toString();
    }

    // ---------------- cadastro ----------------

    @Test
    void cadastro_sendsEmail() throws Exception {
        EmailCadastroRequestDto dto = new EmailCadastroRequestDto();
        dto.setNome("Maria");
        dto.setEmail("maria@example.com");
        dto.setSenha("S3nh@Gerada123");
        dto.setCargo("Analista");
        dto.setArea("Financeiro");
        dto.setNomeEmpresa("Human Consulting");

        emailNotifier.cadastro(dto);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void cadastro_bodyContainsGeneratedPassword() throws Exception {
        EmailCadastroRequestDto dto = new EmailCadastroRequestDto();
        dto.setNome("Maria");
        dto.setEmail("maria@example.com");
        dto.setSenha("S3nh@Gerada123");
        dto.setCargo("Analista");
        dto.setArea("Financeiro");
        dto.setNomeEmpresa("Human Consulting");

        MimeMessage[] captured = new MimeMessage[1];
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> captured[0] = newRealMimeMessage());

        emailNotifier.cadastro(dto);

        String body = extractText(captured[0].getContent());
        assertThat(body).contains("S3nh@Gerada123");
        assertThat(body).contains("maria@example.com");
    }

    @Test
    void cadastro_htmlEscapesUserControlledFields() throws Exception {
        EmailCadastroRequestDto dto = new EmailCadastroRequestDto();
        dto.setNome("<script>alert(1)</script>");
        dto.setEmail("hacker@example.com");
        dto.setSenha("senha123");
        dto.setCargo("<b>Admin</b>");
        dto.setArea("TI");
        dto.setNomeEmpresa("Human Consulting");

        MimeMessage[] captured = new MimeMessage[1];
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> captured[0] = newRealMimeMessage());

        emailNotifier.cadastro(dto);

        String body = extractText(captured[0].getContent());
        assertThat(body).doesNotContain("<script>alert(1)</script>");
        assertThat(body).doesNotContain("<b>Admin</b>");
        assertThat(body).contains("&lt;script&gt;");
        assertThat(body).contains("&lt;b&gt;Admin&lt;/b&gt;");
    }

    // ---------------- update ----------------

    @Test
    void update_sendsEmail() {
        EmailUpdateRequestDto dto = new EmailUpdateRequestDto();
        dto.setDescricaoProjeto("Projeto X");
        dto.setDescricaoSprint("Sprint 1");
        dto.setDescricaoTarefa("Tarefa 1");
        dto.setNomeResponsavelTarefa("João");
        dto.setEmailResponsavelTarefa("joao@example.com");
        dto.setNomeResponsavelProjeto("Ana");
        dto.setEmailResponsavelProjeto("ana@example.com");
        dto.setComentario("Comentário qualquer");
        dto.setComImpedimento(false);

        emailNotifier.update(dto);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void update_nullEmailResponsavelTarefa_doesNotAddNullRecipient() throws Exception {
        EmailUpdateRequestDto dto = new EmailUpdateRequestDto();
        dto.setDescricaoProjeto("Projeto X");
        dto.setDescricaoSprint("Sprint 1");
        dto.setDescricaoTarefa("Tarefa 1");
        dto.setNomeResponsavelTarefa("João");
        dto.setEmailResponsavelTarefa(null); // regression: used to blow up / add a null recipient
        dto.setNomeResponsavelProjeto("Ana");
        dto.setEmailResponsavelProjeto("ana@example.com");
        dto.setComentario("Comentário qualquer");
        dto.setComImpedimento(false);

        MimeMessage[] captured = new MimeMessage[1];
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> captured[0] = newRealMimeMessage());

        emailNotifier.update(dto);

        verify(mailSender).send(any(MimeMessage.class));
        jakarta.mail.Address[] recipients = captured[0].getAllRecipients();
        assertThat(recipients).isNotNull();
        assertThat(recipients).doesNotContainNull();
        assertThat(recipients).hasSize(1);
        assertThat(recipients[0].toString()).contains("ana@example.com");
    }

    @Test
    void update_duplicateResponsavelEmails_notAddedTwice() throws Exception {
        EmailUpdateRequestDto dto = new EmailUpdateRequestDto();
        dto.setDescricaoProjeto("Projeto X");
        dto.setDescricaoSprint("Sprint 1");
        dto.setDescricaoTarefa("Tarefa 1");
        dto.setNomeResponsavelTarefa("Ana");
        dto.setEmailResponsavelTarefa("ana@example.com");
        dto.setNomeResponsavelProjeto("Ana");
        dto.setEmailResponsavelProjeto("ana@example.com");
        dto.setComentario("Comentário qualquer");
        dto.setComImpedimento(true);

        MimeMessage[] captured = new MimeMessage[1];
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> captured[0] = newRealMimeMessage());

        emailNotifier.update(dto);

        jakarta.mail.Address[] recipients = captured[0].getAllRecipients();
        assertThat(recipients).hasSize(1);
    }

    // ---------------- codigo ----------------

    @Test
    void codigo_sendsEmail() {
        UsuarioEnviarCodigoRequestDto dto = new UsuarioEnviarCodigoRequestDto();
        dto.setEmail("user@example.com");
        dto.setCodigo("A1B2C3");

        emailNotifier.codigo(dto);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void codigo_bodyContainsAllSixDigits() throws Exception {
        UsuarioEnviarCodigoRequestDto dto = new UsuarioEnviarCodigoRequestDto();
        dto.setEmail("user@example.com");
        dto.setCodigo("123456");

        MimeMessage[] captured = new MimeMessage[1];
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> captured[0] = newRealMimeMessage());

        emailNotifier.codigo(dto);

        String body = extractText(captured[0].getContent());
        for (char c : "123456".toCharArray()) {
            assertThat(body).contains(String.valueOf(c));
        }
    }

    @Test
    void codigo_nullCodigo_throwsIllegalArgumentException() {
        UsuarioEnviarCodigoRequestDto dto = new UsuarioEnviarCodigoRequestDto();
        dto.setEmail("user@example.com");
        dto.setCodigo(null);

        assertThatThrownBy(() -> emailNotifier.codigo(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void codigo_wrongLengthCodigo_throwsIllegalArgumentException() {
        UsuarioEnviarCodigoRequestDto dto = new UsuarioEnviarCodigoRequestDto();
        dto.setEmail("user@example.com");
        dto.setCodigo("123"); // not 6 chars

        assertThatThrownBy(() -> emailNotifier.codigo(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------------- charset (UTF-8) regression ----------------

    @Test
    void update_declaresUtf8CharsetAndRoundTripsAccentedCharacters() throws Exception {
        EmailUpdateRequestDto dto = new EmailUpdateRequestDto();
        dto.setDescricaoProjeto("Não é possível avançar");
        dto.setDescricaoSprint("Sprint da área de operação");
        dto.setDescricaoTarefa("Revisão de código");
        dto.setNomeResponsavelTarefa("José");
        dto.setEmailResponsavelTarefa(null);
        dto.setNomeResponsavelProjeto("Ana");
        dto.setEmailResponsavelProjeto("ana@example.com");
        dto.setComentario("Situação crítica: não há solução imediata");
        dto.setComImpedimento(false);

        MimeMessage[] captured = new MimeMessage[1];
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> captured[0] = newRealMimeMessage());

        emailNotifier.update(dto);

        // Force JavaMail to finalize/serialize the MIME headers (Content-Type only reliably
        // reflects the real charset param after this) before inspecting them.
        captured[0].saveChanges();
        String contentTypes = collectContentTypes(captured[0]);
        assertThat(contentTypes.toLowerCase()).contains("utf-8");

        String body = extractText(captured[0].getContent());
        assertThat(body).contains("Não é possível avançar");
        assertThat(body).contains("área");
        assertThat(body).contains("José");
        assertThat(body).contains("crítica");
    }
}
