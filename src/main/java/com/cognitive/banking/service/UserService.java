package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.UserRole;
import com.cognitive.banking.domain.enums.UserStatus;
import com.cognitive.banking.dto.CreateUserRequest;
import com.cognitive.banking.dto.UpdateUserRequest;
import com.cognitive.banking.dto.UserDTO;
import com.cognitive.banking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // In‑memory store for failed login attempts (replace with Redis in production)
    private final ConcurrentHashMap<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> firstFailureTime = new ConcurrentHashMap<>();

    @Value("${security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    @Value("${security.password-expiry-days:90}")
    private int passwordExpiryDays;

    private static final String USER_CACHE = "users";
    private static final long CACHE_TTL = 30; // minutes

    // ============================
    // CREATE USER
    // ============================
    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "users", key = "'email:' + #request.email")
    })
    public UserDTO createUser(CreateUserRequest request) {
        logger.info("Creating new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("User creation failed - email already exists: {}", request.getEmail());
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            logger.warn("User creation failed - phone number already exists: {}", request.getPhoneNumber());
            throw new RuntimeException("User with phone number " + request.getPhoneNumber() + " already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Directly use enums from request (CreateUserRequest now uses enums)
        user.setStatus(request.getStatus());
        user.setRole(request.getRole());

        // Set password expiry (default 90 days from now)
        user.setPasswordExpiryDate(LocalDateTime.now().plusDays(passwordExpiryDays));

        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getUserId());

        UserDTO dto = convertToDTO(savedUser);

        cacheService.put(USER_CACHE, savedUser.getUserId().toString(), dto, CACHE_TTL);
        cacheService.put(USER_CACHE, "email:" + savedUser.getEmail(), dto, CACHE_TTL);

        return dto;
    }

    // ============================
    // AUTHENTICATION with Lockout & Password Expiry
    // ============================
    @Transactional(readOnly = true)
    public UserDTO authenticate(String email, String password) {
        logger.info("Authenticating user with email: {}", email);

        // 1. Check if account is temporarily locked (in-memory)
        if (isAccountTemporarilyLocked(email)) {
            logger.warn("Authentication denied - account temporarily locked for email: {}", email);
            throw new RuntimeException("Account temporarily locked due to too many failed attempts. Try again later.");
        }

        // 2. Find user (including locked ones, to record attempts)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    recordFailedAttempt(email);
                    logger.warn("Authentication failed - user not found: {}", email);
                    return new RuntimeException("Invalid credentials");
                });

        // 3. Check permanent lock or status (active / not deleted)
        if (!user.isAccountNonLocked()) {
            logger.warn("Authentication denied - account permanently locked for user: {}", email);
            throw new RuntimeException("Account is locked. Contact support.");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            logger.warn("Authentication denied - inactive account: {}", email);
            throw new RuntimeException("Account is not active.");
        }

        // 4. Check password expiry
        if (!user.isCredentialsNonExpired()) {
            logger.warn("Authentication denied - password expired for user: {}", email);
            throw new RuntimeException("Password expired. Please reset your password.");
        }

        // 5. Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            recordFailedAttempt(email);
            logger.warn("Authentication failed - invalid password for email: {}", email);
            throw new RuntimeException("Invalid credentials");
        }

        // 6. Success: clear failed attempts, update last login
        clearFailedAttempts(email);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        logger.info("Authentication successful for user ID: {}", user.getUserId());
        return convertToDTO(user);
    }

    // Helper: record failed attempt (in-memory; replace with Redis for production)
    private void recordFailedAttempt(String email) {
        int attempts = failedAttempts.getOrDefault(email, 0) + 1;
        failedAttempts.put(email, attempts);
        if (!firstFailureTime.containsKey(email)) {
            firstFailureTime.put(email, LocalDateTime.now());
        }
        if (attempts >= maxFailedAttempts) {
            // Lock the account temporarily in DB
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
            userRepository.lockAccountTemporarily(email, lockUntil);
            logger.warn("Account locked for {} minutes due to {} failed attempts", lockDurationMinutes, attempts);
        }
    }

    private void clearFailedAttempts(String email) {
        failedAttempts.remove(email);
        firstFailureTime.remove(email);
        // Also ensure DB lock is cleared if it was temporary and expired
        userRepository.unlockExpiredAccounts(LocalDateTime.now());
    }

    private boolean isAccountTemporarilyLocked(String email) {
        Integer attempts = failedAttempts.get(email);
        if (attempts == null) return false;
        LocalDateTime firstFail = firstFailureTime.get(email);
        if (firstFail == null) return false;
        // If lock duration has passed, clear attempts
        if (firstFail.plusMinutes(lockDurationMinutes).isBefore(LocalDateTime.now())) {
            clearFailedAttempts(email);
            return false;
        }
        return attempts >= maxFailedAttempts;
    }

    // ============================
    // ACCOUNT LOCK MANAGEMENT
    // ============================
    public void unlockAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        int updated = userRepository.unlockAccount(userId);
        if (updated == 0) {
            throw new RuntimeException("Failed to unlock account");
        }
        clearFailedAttempts(user.getEmail());
        logger.info("Account unlocked for user ID: {}", userId);
    }

    public void lockAccountTemporarily(UUID userId, int minutes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.lockTemporarily(minutes);
        userRepository.save(user);
        logger.info("Account temporarily locked for user ID: {}", userId);
    }

    public void lockAccountPermanently(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.lockPermanently();
        userRepository.save(user);
        logger.info("Account permanently locked for user ID: {}", userId);
    }

    // ============================
    // PASSWORD MANAGEMENT
    // ============================
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        String encodedNew = passwordEncoder.encode(newPassword);
        LocalDateTime newExpiry = LocalDateTime.now().plusDays(passwordExpiryDays);
        userRepository.updatePassword(userId, encodedNew, newExpiry);
        // Clear cache for this user
        cacheService.evict(USER_CACHE, userId.toString());
        cacheService.evict(USER_CACHE, "email:" + user.getEmail());
        logger.info("Password changed for user ID: {}", userId);
    }

    // ============================
    // MFA SECRET MANAGEMENT
    // ============================
    public void enableMfa(UUID userId, String secret) {
        userRepository.setMfaSecret(userId, secret);
        logger.info("MFA enabled for user ID: {}", userId);
    }

    public void disableMfa(UUID userId) {
        userRepository.clearMfaSecret(userId);
        logger.info("MFA disabled for user ID: {}", userId);
    }

    // ============================
    // GET USER BY ID
    // ============================
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(UUID userId) {
        logger.debug("Fetching user by ID: {}", userId);
        return userRepository.findById(userId)
                .map(this::convertToDTO);
    }

    // ============================
    // GET USER BY EMAIL (only for admins, not exposed publicly)
    // ============================
    @Cacheable(value = "users", key = "'email:' + #email", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(this::convertToDTO);
    }

    // ============================
    // GET ALL USERS (admin only, excludes soft-deleted)
    // ============================
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAllNotDeleted().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============================
    // UPDATE USER
    // ============================
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "users", key = "'email:' + #result.email", condition = "#result != null")
    })
    public UserDTO updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        // Role and status updates (admin only in controller)
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        User updated = userRepository.save(user);
        UserDTO dto = convertToDTO(updated);
        cacheService.put(USER_CACHE, userId.toString(), dto, CACHE_TTL);
        cacheService.put(USER_CACHE, "email:" + dto.getEmail(), dto, CACHE_TTL);
        return dto;
    }

    // ============================
    // UPDATE STATUS (simple status change)
    // ============================
    public UserDTO updateUserStatus(UUID userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.valueOf(status));
        User updated = userRepository.save(user);
        return convertToDTO(updated);
    }

    // ============================
    // SOFT DELETE USER
    // ============================
    public void softDeleteUser(UUID userId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        int updated = userRepository.softDeleteUser(userId, timestamp);
        if (updated == 0) {
            throw new RuntimeException("User not found");
        }
        cacheService.evict(USER_CACHE, userId.toString());
        logger.info("User soft deleted: {}", userId);
    }

    // ============================
    // CONVERTER
    // ============================
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus().name(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }

    public long getActiveUsersCount() {
        return userRepository.countByStatus(UserStatus.ACTIVE);
    }

    // Scheduled job to unlock expired temporary locks (call via @Scheduled)
    @Transactional
    public void unlockExpiredAccountsJob() {
        int unlocked = userRepository.unlockExpiredAccounts(LocalDateTime.now());
        if (unlocked > 0) {
            logger.info("Auto-unlocked {} accounts", unlocked);
        }
    }

    // In UserService.java
    public boolean isMfaEnabled(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return user.getMfaSecret() != null && !user.getMfaSecret().isBlank();
    }
}