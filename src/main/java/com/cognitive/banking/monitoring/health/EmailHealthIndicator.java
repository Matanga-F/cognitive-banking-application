package com.cognitive.banking.monitoring.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailHealthIndicator implements HealthIndicator {
    private final JavaMailSender mailSender;

    public EmailHealthIndicator(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public Health health() {
        try {
            // Test connection by getting session
            mailSender.createMimeMessage();
            return Health.up()
                    .withDetail("service", "email")
                    .withDetail("status", "available")
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("service", "email")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}