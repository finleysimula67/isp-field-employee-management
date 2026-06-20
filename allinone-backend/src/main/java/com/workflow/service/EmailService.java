package com.workflow.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendPasswordResetEmail(String to, String resetLink) {
        sendHtmlEmail(to, "Reset your All in One & Network Solutions password",
                "<p>You requested a password reset.</p>"
                + "<p>Click the link below to reset your password (expires in 1 hour):</p>"
                + "<p><a href=\"" + resetLink + "\">" + resetLink + "</a></p>"
                + "<p>If you didn't request this, ignore this email.</p>");
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
        sendHtmlEmail(to, subject, "<p>" + body.replace("\n", "<br>") + "</p>");
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            String wrapper = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\">"
                    + "<div style=\"background: #2563eb; color: white; padding: 20px; text-align: center; font-size: 20px;\">"
                    + "All in One &amp; Network Solutions</div>"
                    + "<div style=\"padding: 20px; border: 1px solid #e5e7eb;\">"
                    + htmlContent
                    + "</div></div>";
            helper.setText(wrapper, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }
}
