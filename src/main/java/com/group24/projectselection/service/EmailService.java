package com.group24.projectselection.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.Optional;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:noreply@xjtlu.edu.cn}")
    private String fromAddress;

    /**
     * When {@code spring.mail.host} (and related mail props) are not set, Spring Boot
     * does not register a {@link JavaMailSender} bean. Optional injection keeps the
     * application context startable for local development without SMTP.
     */
    public EmailService(Optional<JavaMailSender> mailSender) {
        this.mailSender = mailSender.orElse(null);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured (missing spring.mail.*); "
                    + "skipping password reset email to {} (use reset-password flow manually in dev).",
                    toEmail);
            return;
        }

        String resetUrl = baseUrl + "/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String body = """
                <p>Hello,</p>
                <p>You have requested to reset your password.</p>
                <p>Click the link below to set a new password (link expires in 30 minutes):</p>
                <p><a href="%s">Reset Password</a></p>
                <p>If you did not request this, you can safely ignore this email.</p>
                """.formatted(resetUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}", toEmail, e);
        }
    }
}
