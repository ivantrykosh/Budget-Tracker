package com.ivantrykosh.app.budgettracker.server.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service class for sending emails using JavaMailSender.
 */
@Service
public class EmailSenderService {
    @Autowired
    private JavaMailSender mailSender; // Mail sender

    @Value("${spring.mail.username}")
    private String from; // From email

    Logger logger = LoggerFactory.getLogger(EmailSenderService.class); // Logger

    /**
     * Asynchronously sends an email.
     *
     * @param to      The recipient's email address.
     * @param subject The subject of the email.
     * @param body    The body/content of the email.
     */
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setFrom(from);

            mailSender.send(mimeMessage);

            logger.info("Email with subject " + subject + " was sent to " + to);
        } catch (MessagingException e) {
            logger.error("Failed to send email with subject " + subject + " to " + to);
            throw new IllegalArgumentException("Failed to send email!");
        }
    }
}
