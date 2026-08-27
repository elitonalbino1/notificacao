package com.javanauta.notificacao.business;

import com.javanauta.notificacao.business.dto.TarefasDTO;
import com.javanauta.notificacao.infrastructure.exceptions.EmailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${envio.email.remetente}")
    private String remetente;

    @Value("${envio.email.nomeRemetente}")
    private String nomeRemetente;

    @Async
    public void enviaEmail(TarefasDTO dto) {
        try {
            log.info("Enviando email para: {}", dto.getEmailUsuario());

            MimeMessage mensagem = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(remetente, nomeRemetente));
            helper.setTo(InternetAddress.parse(dto.getEmailUsuario()));
            helper.setSubject("Notificação de Tarefa");

            Context context = new Context();
            context.setVariable("nomeTarefa", dto.getNomeTarefa());
            context.setVariable("dataEvento", dto.getDataEvento());
            context.setVariable("descricaoTarefa", dto.getDescricaoTarefa());
            String template = templateEngine.process("notificacao", context);
            helper.setText(template, true);

            javaMailSender.send(mensagem);
            log.info("Email enviado com sucesso para: {}", dto.getEmailUsuario());

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Erro ao enviar email para: {}", dto.getEmailUsuario(), e);
            throw new EmailException("Erro ao enviar o email", e);
        }
    }
}
