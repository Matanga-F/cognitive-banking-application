package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.UserRole;
import com.cognitive.banking.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ==================== BASIC CRUD & AUTHENTICATION ====================

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    // Strict authentication: only active, non‑locked accounts
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status = 'ACTIVE' AND u.locked = false")
    Optional<User> findActiveByEmail(@Param("email") String email);

    // For admin search
    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    // ==================== STATUS & ROLE QUERIES ====================

    long countByStatus(UserStatus status);

    long countByRole(UserRole role);

    List<User> findAllByStatus(UserStatus status);

    List<User> findAllByRole(UserRole role);

    Page<User> findAllByStatus(UserStatus status, Pageable pageable);

    // ==================== ACCOUNT LOCKING (BRUTE FORCE PROTECTION) ====================

    /**
     * Temporarily lock an account after too many failed login attempts.
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.locked = true, u.lockedUntil = :lockedUntil WHERE u.email = :email")
    int lockAccountTemporarily(@Param("email") String email, @Param("lockedUntil") LocalDateTime lockedUntil);

    /**
     * Permanently lock an account (e.g., admin action).
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.locked = true, u.lockedUntil = null WHERE u.email = :email")
    int lockAccountPermanently(@Param("email") String email);

    /**
     * Manually unlock an account.
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.locked = false, u.lockedUntil = null WHERE u.userId = :userId")
    int unlockAccount(@Param("userId") UUID userId);

    /**
     * Auto‑unlock all accounts whose temporary lock period has expired.
     * Called by a scheduled job (e.g., every minute).
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.locked = false, u.lockedUntil = null WHERE u.locked = true AND u.lockedUntil IS NOT NULL AND u.lockedUntil < :now")
    int unlockExpiredAccounts(@Param("now") LocalDateTime now);

    // ==================== PASSWORD MANAGEMENT ====================

    /**
     * Change password and set new expiry date.
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :newPassword, u.passwordExpiryDate = :expiryDate WHERE u.userId = :userId")
    int updatePassword(@Param("userId") UUID userId,
                       @Param("newPassword") String newPassword,
                       @Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Find all active users whose password has expired.
     */
    @Query("SELECT u FROM User u WHERE u.passwordExpiryDate IS NOT NULL AND u.passwordExpiryDate < :now AND u.status = 'ACTIVE'")
    List<User> findUsersWithExpiredPasswords(@Param("now") LocalDateTime now);

    // ==================== ACCOUNT EXPIRY (DORMANCY) ====================

    /**
     * Find active accounts that have passed their account expiry date.
     */
    @Query("SELECT u FROM User u WHERE u.accountExpiryDate IS NOT NULL AND u.accountExpiryDate < :now AND u.status = 'ACTIVE'")
    List<User> findExpiredAccounts(@Param("now") LocalDateTime now);

    /**
     * Automatically mark expired accounts as INACTIVE (or DELETED).
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = :newStatus WHERE u.accountExpiryDate IS NOT NULL AND u.accountExpiryDate < :now AND u.status = 'ACTIVE'")
    int deactivateExpiredAccounts(@Param("now") LocalDateTime now, @Param("newStatus") UserStatus newStatus);

    // ==================== LAST LOGIN TRACKING ====================

    /**
     * Update lastLoginAt timestamp without loading full entity.
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginAt = :lastLogin WHERE u.email = :email")
    int updateLastLogin(@Param("email") String email, @Param("lastLogin") LocalDateTime lastLogin);

    // ==================== MFA (TOTP) ====================

    /**
     * Store TOTP secret for a user.
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.mfaSecret = :secret WHERE u.userId = :userId")
    int setMfaSecret(@Param("userId") UUID userId, @Param("secret") String secret);

    /**
     * Clear MFA secret (disable MFA).
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.mfaSecret = null WHERE u.userId = :userId")
    int clearMfaSecret(@Param("userId") UUID userId);

    // ==================== ADMIN & MONITORING ====================

    /**
     * Count users locked temporarily.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.locked = true AND u.lockedUntil IS NOT NULL AND u.lockedUntil > :now")
    long countTemporarilyLockedAccounts(@Param("now") LocalDateTime now);

    /**
     * Count users locked permanently.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.locked = true AND u.lockedUntil IS NULL")
    long countPermanentlyLockedAccounts();

    /**
     * Find all locked accounts (for admin dashboards).
     */
    @Query("SELECT u FROM User u WHERE u.locked = true")
    List<User> findAllLockedAccounts();

    // ==================== SOFT DELETE ====================

    /**
     * Soft delete a user (set status to DELETED).
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = 'DELETED', u.email = CONCAT(u.email, '.deleted.', :timestamp) WHERE u.userId = :userId")
    int softDeleteUser(@Param("userId") UUID userId, @Param("timestamp") String timestamp);

    /**
     * Exclude soft‑deleted users from normal queries.
     */
    @Query("SELECT u FROM User u WHERE u.status != 'DELETED'")
    List<User> findAllNotDeleted();

    // ==================== BULK OPERATIONS (for scheduled jobs) ====================

    /**
     * Bulk unlock all temporarily locked accounts (used by scheduled job).
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.locked = false, u.lockedUntil = null WHERE u.locked = true AND u.lockedUntil IS NOT NULL AND u.lockedUntil < :now")
    int bulkUnlockExpired(@Param("now") LocalDateTime now);
}