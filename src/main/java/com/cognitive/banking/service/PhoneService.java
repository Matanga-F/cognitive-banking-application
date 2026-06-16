package com.cognitive.banking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PhoneService {
    private static final Logger logger = LoggerFactory.getLogger(PhoneService.class);

    private final CacheService cacheService;

    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${sms.provider:twilio}")
    private String smsProvider;

    @Value("${sms.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${sms.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${sms.twilio.phone-number:}")
    private String twilioPhoneNumber;

    @Value("${app.default-country-code:+1}")
    private String defaultCountryCode;

    private final SecureRandom secureRandom = new SecureRandom();

    public PhoneService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Generate and send OTP to phone number
     * @param phoneNumber The phone number (with country code)
     * @return The generated OTP (for testing/fallback)
     */
    public String generateAndSendOtp(String phoneNumber) {
        String normalizedNumber = normalizePhoneNumber(phoneNumber);
        String otp = generateOtp(6);

        // Store OTP in cache with 10 minute expiry
        cacheService.savePhoneOtp(normalizedNumber, otp, 10);

        // Send SMS asynchronously
        CompletableFuture.runAsync(() -> sendSms(normalizedNumber, otp));

        logger.info("OTP generated and sending to: {}", maskPhoneNumber(normalizedNumber));
        return otp;
    }

    /**
     * Verify OTP for phone number
     */
    public boolean verifyOtp(String phoneNumber, String otp) {
        String normalizedNumber = normalizePhoneNumber(phoneNumber);
        boolean isValid = cacheService.validatePhoneOtp(normalizedNumber, otp);

        if (isValid) {
            cacheService.deleteOtp(normalizedNumber);
            logger.info("OTP verified successfully for: {}", maskPhoneNumber(normalizedNumber));
        } else {

            logger.warn("Invalid OTP attempt for: {}", maskPhoneNumber(normalizedNumber));
        }

        return isValid;
    }

    /**
     * Send welcome SMS to new user
     */
    public void sendWelcomeSms(String phoneNumber, String firstName) {
        String normalizedNumber = normalizePhoneNumber(phoneNumber);
        String message = String.format("Welcome %s to Cognitive Banking! Your account has been successfully created.", firstName);
        sendSms(normalizedNumber, message);
        logger.info("Welcome SMS sent to: {}", maskPhoneNumber(normalizedNumber));
    }

    /**
     * Send transaction alert SMS
     */
    public void sendTransactionAlert(String phoneNumber, String transactionType, String amount, String accountNumber) {
        String normalizedNumber = normalizePhoneNumber(phoneNumber);
        String message = String.format("Transaction Alert: %s of %s on account ****%s has been processed.",
                transactionType, amount, getLastFourDigits(accountNumber));
        sendSms(normalizedNumber, message);
        logger.info("Transaction alert SMS sent to: {}", maskPhoneNumber(normalizedNumber));
    }

    /**
     * Send security alert SMS
     */
    public void sendSecurityAlert(String phoneNumber, String alertType) {
        String normalizedNumber = normalizePhoneNumber(phoneNumber);
        String message = String.format("Security Alert: %s detected on your account. If this was not you, please contact support immediately.", alertType);
        sendSms(normalizedNumber, message);
        logger.info("Security alert SMS sent to: {}", maskPhoneNumber(normalizedNumber));
    }

    /**
     * Send low balance alert SMS
     */
    public void sendLowBalanceAlert(String phoneNumber, String balance, String accountNumber) {
        String normalizedNumber = normalizePhoneNumber(phoneNumber);
        String message = String.format("Low Balance Alert: Your account ****%s balance is %s. Please ensure sufficient funds.",
                getLastFourDigits(accountNumber), balance);
        sendSms(normalizedNumber, message);
        logger.info("Low balance alert SMS sent to: {}", maskPhoneNumber(normalizedNumber));
    }

    /**
     * Send payment confirmation SMS
     */
    public void sendPaymentConfirmation(String phoneNumber, String amount, String reference) {
        String normalizedNumber = normalizePhoneNumber(phoneNumber);
        String message = String.format("Payment Confirmation: %s has been processed. Reference: %s", amount, reference);
        sendSms(normalizedNumber, message);
        logger.info("Payment confirmation SMS sent to: {}", maskPhoneNumber(normalizedNumber));
    }

    // ============================
    // SMS SENDING IMPLEMENTATION
    // ============================

    private void sendSms(String phoneNumber, String message) {
        if (!smsEnabled) {
            logger.debug("SMS disabled. Would send to {}: {}", phoneNumber, message);
            return;
        }

        try {
            if ("twilio".equalsIgnoreCase(smsProvider)) {
                sendViaTwilio(phoneNumber, message);
            } else if ("aws".equalsIgnoreCase(smsProvider)) {
                sendViaAwsSns(phoneNumber, message);
            } else {
                sendViaMock(phoneNumber, message);
            }
            logger.debug("SMS sent successfully to: {}", maskPhoneNumber(phoneNumber));
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", maskPhoneNumber(phoneNumber), e.getMessage());
        }
    }

    private void sendViaTwilio(String phoneNumber, String message) {
        // Twilio implementation - uncomment when twilio SDK is added
        /*
        Twilio.init(twilioAccountSid, twilioAuthToken);
        Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(twilioPhoneNumber),
                message
        ).create();
        */
        logger.debug("Sending via Twilio to {}: {}", phoneNumber, message);
    }

    private void sendViaAwsSns(String phoneNumber, String message) {
        // AWS SNS implementation
        logger.debug("Sending via AWS SNS to {}: {}", phoneNumber, message);
    }

    private void sendViaMock(String phoneNumber, String message) {
        // Mock implementation for testing
        logger.info("MOCK SMS - To: {}, Message: {}", phoneNumber, message);
    }

    // ============================
    // UTILITY METHODS
    // ============================

    private String generateOtp(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        String normalized = phoneNumber.trim();

        // Add default country code if missing
        if (!normalized.startsWith("+")) {
            if (normalized.startsWith("00")) {
                normalized = "+" + normalized.substring(2);
            } else if (normalized.startsWith("0")) {
                normalized = defaultCountryCode + normalized.substring(1);
            } else {
                normalized = defaultCountryCode + normalized;
            }
        }

        // Remove all non-digit characters except '+'
        normalized = normalized.replaceAll("[^\\d+]", "");

        return normalized;
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return "***";
        }
        String lastFour = phoneNumber.substring(Math.max(0, phoneNumber.length() - 4));
        String prefix = phoneNumber.length() > 4 ? phoneNumber.substring(0, Math.min(3, phoneNumber.length() - 4)) : "";
        return prefix + "****" + lastFour;
    }

    private String getLastFourDigits(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return accountNumber.substring(accountNumber.length() - 4);
    }
}