package com.projecta.notification.service;

import com.projecta.notification.dto.NotificationEvent;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void send(NotificationEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(event.getRecipientEmail());
            helper.setSubject(event.getSubject());

            String htmlBody;
            if (event.getTemplateId() != null && !event.getTemplateId().isBlank()) {
                htmlBody = renderTemplate(event.getTemplateId(), event.getTemplateVars());
            } else {
                htmlBody = event.getBody();
            }

            helper.setText(htmlBody, true);
            mailSender.send(message);

            log.info("Email sent to {} for eventId={}", event.getRecipientEmail(), event.getEventId());

        } catch (Exception e) {
            log.error("Email send failed to {} for eventId={}: {}",
                    event.getRecipientEmail(), event.getEventId(), e.getMessage());
            throw new RuntimeException("Email send failed: " + e.getMessage(), e);
        }
    }

    private String renderTemplate(String templateId, Map<String, String> vars) {
        Context context = new Context();
        if (vars != null) {
            vars.forEach(context::setVariable);
        }
        return templateEngine.process("email/" + templateId, context);
    }
}
