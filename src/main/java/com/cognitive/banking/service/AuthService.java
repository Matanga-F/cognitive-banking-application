package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.UserRole;
import com.cognitive.banking.domain.enums.UserStatus;
import com.cognitive.banking.dto.*;
import com.cognitive.banking.monitoring.metrics.BankingMetrics;
import com.cognitive.banking.monitoring.metrics.MetricNames;
import com.cognitive.banking.monitoring.metrics.MetricTags;
import com.cognitive.banking.repository.UserRepository;
import com.cognitive.banking.security.JwtUtil;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BankingMetrics bankingMetrics;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ============================================
    // REGISTER - With Full Metrics
    // ============================================
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering user: {}", request.getEmail());
        Timer.Sample sample = bankingMetrics.startRegistrationTimer();

        try {
            // Validate email
            if (userRepository.existsByEmail(request.getEmail())) {
                bankingMetrics.recordUserRegistration(false, MetricTags.ERROR_DUPLICATE_EMAIL);
                throw new RuntimeException("Email already exists");
            }

            // Validate username
            if (userRepository.existsByUsername(request.getUsername())) {
                bankingMetrics.recordUserRegistration(false, "duplicate_username");
                throw new RuntimeException("Username already exists");
            }

            // Create user
            User user = new User();
            user.setUserId(user.getUserId());
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setRole(request.getRole() != null ? request.getRole() : UserRole.CUSTOMER);
            user.setStatus(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setEmailVerified(false);
            user.setPhoneVerified(false);
            user.setTwoFactorEnabled(false);
            user.setLocked(false);

            User savedUser = userRepository.save(user);
            String token = jwtUtil.generateToken(user.getEmail());

            // Record success metrics
            bankingMetrics.recordUserRegistration(true, null);
//            bankingMetrics.recordUserByRole(user.getRole().name());
            bankingMetrics.updateUserCounts(
                    userRepository.count(),
                    userRepository.countByStatus(UserStatus.ACTIVE),
                    userRepository.countByRole(UserRole.ADMIN),
                    userRepository.countByRole(UserRole.MANAGER),
                    userRepository.countByRole(UserRole.CUSTOMER)
            );

            // Stop timer
            bankingMetrics.stopRegistrationTimer(sample);

            logger.info("User registered successfully: {} (Total users: {})",
                    user.getEmail(), bankingMetrics.getCurrentTotalUsers());

            return buildAuthResponse(savedUser, token);

        } catch (Exception e) {
            bankingMetrics.stopRegistrationTimer(sample);
            logger.error("Registration failed for {}: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    // ============================================
    // LOGIN - With Full Metrics
    // ============================================
    public AuthResponse login(LoginRequest request) {
        logger.info("Authenticating user: {}", request.getEmail());
        Timer.Sample sample = bankingMetrics.startLoginTimer();

        try {
            // Find user
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        bankingMetrics.recordUserLogin(false, MetricTags.ERROR_USER_NOT_FOUND);
                        return new RuntimeException("User not found");
                    });

            // Check password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                bankingMetrics.recordUserLogin(false, MetricTags.ERROR_INVALID_CREDENTIALS);
                throw new RuntimeException("Invalid password");
            }

            // Check if account is locked
            if (user.isLocked()) {
                bankingMetrics.recordUserLogin(false, "account_locked");
                throw new RuntimeException("Account is locked");
            }

            // Update last login
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Generate token
            String token = jwtUtil.generateToken(user.getEmail());

            // Record success metrics
            bankingMetrics.recordUserLogin(true, null);
            bankingMetrics.recordLoginByRole(user.getRole().name());

            // Stop timer
            bankingMetrics.stopLoginTimer(sample);

            logger.info("Authentication successful for: {} (Role: {})",
                    request.getEmail(), user.getRole().name());

            return buildAuthResponse(user, token);

        } catch (Exception e) {
            bankingMetrics.stopLoginTimer(sample);
            logger.error("Login failed for {}: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    // ============================================
    // AUTHENTICATE (Alias for login)
    // ============================================
    public AuthResponse authenticate(@Valid AuthRequest request) {
        return login(new LoginRequest(request.getEmail(), request.getPassword()));
    }

    // ============================================
    // LOGOUT - With Metrics
    // ============================================
    public void logout(String email) {
        logger.info("User logged out: {}", email);
        bankingMetrics.recordUserLogout();
    }

    public void logout(String email, String token) {
        logger.info("User logged out: {}", email);
        bankingMetrics.recordUserLogout();
    }

    // ============================================
    // REGISTER NEW USER (Admin) - With Metrics
    // ============================================
    public UserDTO registerNewUser(CreateUserRequest request) {
        logger.info("Admin registering new user: {}", request.getEmail());
        Timer.Sample sample = bankingMetrics.startRegistrationTimer();

        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                bankingMetrics.recordUserRegistration(false, MetricTags.ERROR_DUPLICATE_EMAIL);
                throw new RuntimeException("Email already exists");
            }

            User user = new User();
            user.setUserId(user.getUserId());
            user.setEmail(request.getEmail());
            user.setUsername(request.getEmail().split("@")[0]);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setRole(request.getRole());
            user.setStatus(request.getStatus());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            User savedUser = userRepository.save(user);

            // Record metrics
            bankingMetrics.recordUserRegistration(true, null);
//            bankingMetrics.recordUserByRole(user.getRole().name());
            bankingMetrics.updateUserCounts(
                    userRepository.count(),
                    userRepository.countByStatus(UserStatus.ACTIVE),
                    userRepository.countByRole(UserRole.ADMIN),
                    userRepository.countByRole(UserRole.MANAGER),
                    userRepository.countByRole(UserRole.CUSTOMER)
            );
            bankingMetrics.stopRegistrationTimer(sample);

            logger.info("Admin created user: {} with role: {}", savedUser.getEmail(), savedUser.getRole());

            return convertToDTO(savedUser);

        } catch (Exception e) {
            bankingMetrics.stopRegistrationTimer(sample);
            throw e;
        }
    }

    // ============================================
    // CHANGE PASSWORD - With Metrics
    // ============================================
    public void changePassword(String email, String currentPassword, String newPassword) {
        logger.info("Changing password for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            bankingMetrics.recordPasswordChange(false);
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        bankingMetrics.recordPasswordChange(true);
        logger.info("Password changed successfully for: {}", email);
    }

    // ============================================
    // VERIFY EMAIL - With Metrics
    // ============================================
    public void verifyEmail(String token) {
        logger.info("Verifying email with token");
        Timer.Sample sample = bankingMetrics.startEmailVerificationTimer();

        try {
            User user = userRepository.findByEmailVerificationToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid verification token"));

            if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification token has expired");
            }

            user.setEmailVerified(true);
            user.setEmailVerificationToken(null);
            user.setEmailVerificationTokenExpiry(null);
            userRepository.save(user);

            bankingMetrics.recordEmailVerification(true);
            bankingMetrics.updateUserCounts(
                    userRepository.count(),
                    userRepository.countByStatus(UserStatus.ACTIVE),
                    userRepository.countByRole(UserRole.ADMIN),
                    userRepository.countByRole(UserRole.MANAGER),
                    userRepository.countByRole(UserRole.CUSTOMER)
            );
            bankingMetrics.stopEmailVerificationTimer(sample);

            logger.info("Email verified for: {}", user.getEmail());

        } catch (Exception e) {
            bankingMetrics.stopEmailVerificationTimer(sample);
            bankingMetrics.recordEmailVerification(false);
            throw e;
        }
    }

    // ============================================
    // SEND VERIFICATION EMAIL
    // ============================================
    public void sendVerificationEmail(String email) {
        logger.info("Sending verification email to: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusDays(1));
        userRepository.save(user);

        logger.info("Verification token for {}: {}", email, token);
        bankingMetrics.recordEmailSent();
    }

    // ============================================
    // 2FA METHODS - With Metrics
    // ============================================
    public String enableTwoFactorAuth(String email) {
        logger.info("Enabling 2FA for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String secret = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        bankingMetrics.recordTwoFactorChange(true);
//        bankingMetrics.updateTwoFactorEnabledCount(userRepository.countByTwoFactorEnabled(true));

        logger.info("2FA enabled for: {}", email);
        return secret;
    }

    public void disableTwoFactorAuth(String email, String code) {
        logger.info("Disabling 2FA for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (code == null || code.length() != 6) {
            throw new RuntimeException("Invalid 2FA code");
        }

        user.setTwoFactorSecret(null);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);

        bankingMetrics.recordTwoFactorChange(false);
//        bankingMetrics.updateTwoFactorEnabledCount(userRepository.countByTwoFactorEnabled(true));

        logger.info("2FA disabled for: {}", email);
    }

    // ============================================
    // PHONE VERIFICATION - With Metrics
    // ============================================
    public void sendPhoneVerificationOtp(String phoneNumber) {
        logger.info("Sending OTP to: {}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = String.format("%06d", new Random().nextInt(999999));
        logger.info("OTP for {}: {}", phoneNumber, otp);
        bankingMetrics.recordPhoneOtpSent();
    }

    public void verifyPhoneNumber(String phoneNumber, String otp) {
        logger.info("Verifying phone: {}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (otp == null || otp.length() != 6) {
            throw new RuntimeException("Invalid OTP");
        }

        user.setPhoneVerified(true);
        userRepository.save(user);

        bankingMetrics.recordPhoneVerification(true);

        logger.info("Phone verified for: {}", user.getEmail());
    }

    // ============================================
    // PASSWORD RESET - With Metrics
    // ============================================
    public void initiatePasswordReset(String email) {
        logger.info("Initiating password reset for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        logger.info("Password reset token for {}: {}", email, resetToken);
        bankingMetrics.recordPasswordResetInitiated();
    }

    public void resetPassword(String token, String newPassword) {
        logger.info("Resetting password with token");
        Timer.Sample sample = bankingMetrics.startPasswordResetTimer();

        try {
            User user = userRepository.findByResetToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

            if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Reset token has expired");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            user.setPasswordExpiryDate(LocalDateTime.now().plusDays(90));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            bankingMetrics.recordPasswordReset(true);
            bankingMetrics.stopPasswordResetTimer(sample);

            logger.info("Password reset successful for: {}", user.getEmail());

        } catch (Exception e) {
            bankingMetrics.stopPasswordResetTimer(sample);
            bankingMetrics.recordPasswordReset(false);
            throw e;
        }
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private AuthResponse buildAuthResponse(User user, String token) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        response.setTokenType("Bearer");
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmailVerified(user.isEmailVerified());
        response.setPhoneVerified(user.isPhoneVerified());
        response.setTwoFactorEnabled(user.isTwoFactorEnabled());
        return response;
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole().name());
        dto.setStatus(user.getStatus().name());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setPhoneVerified(user.isPhoneVerified());
        dto.setTwoFactorEnabled(user.isTwoFactorEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        return dto;
    }
}