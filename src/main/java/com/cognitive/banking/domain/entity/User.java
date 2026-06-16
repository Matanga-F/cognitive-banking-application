package com.cognitive.banking.domain.entity;

import com.cognitive.banking.domain.enums.UserRole;
import com.cognitive.banking.domain.enums.UserStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.CUSTOMER;

    // ========== Security Enhancements ==========
    @Column(nullable = false)
    private boolean locked = false;                 // brute‑force lock flag

    private LocalDateTime lockedUntil;              // auto‑unlock timestamp (if temporary)

    private LocalDateTime passwordExpiryDate;       // when password must be changed

    private LocalDateTime accountExpiryDate;        // when account becomes dormant

    @Column(length = 64)
    private String mfaSecret;                       // TOTP secret (encrypted in real prod)

    // ========== Timestamps ==========
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    // ========== Password Reset Fields ==========
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expiry")
    private LocalDateTime emailVerificationTokenExpiry;

    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled = false;

    @Column(name = "phone_verified")
    private boolean phoneVerified = false;

    @Column(unique = true, nullable = false)
    private String username;

    // Constructors
    public User() {}

    public User(String firstName, String lastName, String email, String phoneNumber, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ========== Business Methods ==========

    /**
     * Determines if the account is considered non‑locked for authentication.
     * - If status is not ACTIVE → locked.
     * - If locked flag is true and lockedUntil is in the past → auto‑unlock and return true.
     * - If locked flag is true and lockedUntil is future → locked.
     */
    public boolean isAccountNonLocked() {
        if (status != UserStatus.ACTIVE && status != UserStatus.LOCKED) {
            return false;
        }
        if (locked && lockedUntil != null && lockedUntil.isBefore(LocalDateTime.now())) {
            // Auto‑unlock: clear the lock
            this.locked = false;
            this.lockedUntil = null;
            return true;
        }
        return !locked;
    }

    /**
     * Locks the account temporarily (e.g., after 5 failed logins).
     * @param durationMinutes how long to lock
     */
    public void lockTemporarily(int durationMinutes) {
        this.locked = true;
        this.lockedUntil = LocalDateTime.now().plusMinutes(durationMinutes);
        this.status = UserStatus.LOCKED;
    }

    /**
     * Permanently locks the account (admin action or too many temp locks).
     */
    public void lockPermanently() {
        this.locked = true;
        this.lockedUntil = null;
        this.status = UserStatus.LOCKED;
    }

    /**
     * Unlocks the account manually.
     */
    public void unlock() {
        this.locked = false;
        this.lockedUntil = null;
        this.status = UserStatus.ACTIVE;
    }

    public boolean isCredentialsNonExpired() {
        return passwordExpiryDate == null || passwordExpiryDate.isAfter(LocalDateTime.now());
    }

    public boolean isAccountNonExpired() {
        return accountExpiryDate == null || accountExpiryDate.isAfter(LocalDateTime.now());
    }

    public boolean isMfaEnabled() {
        return mfaSecret != null && !mfaSecret.isBlank();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ========== JPA Lifecycle Callbacks ==========
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== Getters and Setters ==========
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public LocalDateTime getPasswordExpiryDate() { return passwordExpiryDate; }
    public void setPasswordExpiryDate(LocalDateTime passwordExpiryDate) { this.passwordExpiryDate = passwordExpiryDate; }

    public LocalDateTime getAccountExpiryDate() { return accountExpiryDate; }
    public void setAccountExpiryDate(LocalDateTime accountExpiryDate) { this.accountExpiryDate = accountExpiryDate; }

    public String getMfaSecret() { return mfaSecret; }
    public void setMfaSecret(String mfaSecret) { this.mfaSecret = mfaSecret; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

    public String getEmailVerificationToken() { return emailVerificationToken; }
    public void setEmailVerificationToken(String emailVerificationToken) { this.emailVerificationToken = emailVerificationToken; }

    public LocalDateTime getEmailVerificationTokenExpiry() { return emailVerificationTokenExpiry; }
    public void setEmailVerificationTokenExpiry(LocalDateTime emailVerificationTokenExpiry) { this.emailVerificationTokenExpiry = emailVerificationTokenExpiry; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getTwoFactorSecret() { return twoFactorSecret; }
    public void setTwoFactorSecret(String twoFactorSecret) { this.twoFactorSecret = twoFactorSecret; }

    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public boolean isPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(boolean phoneVerified) { this.phoneVerified = phoneVerified; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    // Alias methods for compatibility with some services
    public void setPasswordHash(String encodedPassword) {
        this.password = encodedPassword;
    }

    public String getPasswordHash() {
        return this.password;
    }
}