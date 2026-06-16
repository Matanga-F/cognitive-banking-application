package com.cognitive.banking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean emailEnabled;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.email.enabled:false}") boolean emailEnabled) {
        this.mailSender = mailSender;
        this.emailEnabled = emailEnabled;
    }

    void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            logger.info("========== EMAIL (DISABLED) ==========");
            logger.info("To: {}", maskEmail(to));
            logger.info("Subject: {}", subject);
            logger.info("Body: {}", body);
            logger.info("======================================");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            if (fromEmail != null && !fromEmail.isEmpty()) {
                message.setFrom(fromEmail);
            }
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", maskEmail(to));
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", maskEmail(to), e.getMessage());
        }
    }

    // Keep all your existing public methods - they call sendEmail()
    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = baseUrl + "/auth/reset-password?token=" + token;
        String subject = "Password Reset Request - Cognitive Banking";
        String body = String.format(
                "Dear Customer,\n\nClick the link to reset your password: %s\n\nThis link expires in 1 hour.\n\nBest regards,\nCognitive Banking Team",
                resetUrl
        );
        sendEmail(to, subject, body);
    }

    public void sendEmailVerification(String to, String token) {
        String verifyUrl = baseUrl + "/auth/verify-email?token=" + token;
        String subject = "Verify Your Email - Cognitive Banking";
        String body = String.format(
                "Dear Customer,\n\nClick the link to verify your email: %s\n\nThis link expires in 24 hours.\n\nBest regards,\nCognitive Banking Team",
                verifyUrl
        );
        sendEmail(to, subject, body);
    }

    public void sendAccountLockedEmail(String to, long lockoutMinutes) {
        String subject = "Account Locked - Cognitive Banking";
        String body = String.format(
                "Dear Customer,\n\nYour account has been locked for %d minutes due to too many failed attempts.\n\nBest regards,\nCognitive Banking Team",
                lockoutMinutes
        );
        sendEmail(to, subject, body);
    }

    public void sendTransactionAlert(String to, String transactionType, String amount, String accountNumber) {
        String subject = "Transaction Alert - Cognitive Banking";
        String body = String.format(
                "Transaction: %s of %s on account ****%s",
                transactionType, amount, getLastFourDigits(accountNumber)
        );
        sendEmail(to, subject, body);
    }

    public void sendLowBalanceAlert(String to, String balance, String accountNumber) {
        String subject = "Low Balance Alert - Cognitive Banking";
        String body = String.format(
                "Account ****%s balance is %s",
                getLastFourDigits(accountNumber), balance
        );
        sendEmail(to, subject, body);
    }

    public void sendWelcomeEmail(String to, String firstName) {
        String subject = "Welcome to Cognitive Banking!";
        String body = String.format("Dear %s,\n\nWelcome to Cognitive Banking!", firstName);
        sendEmail(to, subject, body);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        if (localPart.length() <= 2) {
            return "***@" + parts[1];
        }
        return localPart.substring(0, 2) + "***@" + parts[1];
    }

    private String getLastFourDigits(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return accountNumber.substring(accountNumber.length() - 4);
    }
}