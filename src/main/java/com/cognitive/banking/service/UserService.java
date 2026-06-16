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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@Primary
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final CacheService cacheService;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.password-expiry-days:90}")
    private int passwordExpiryDays;

    public UserService(UserRepository userRepository, CacheService cacheService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cacheService = cacheService;
        this.passwordEncoder = passwordEncoder;
    }

    private static final String USER_CACHE = "users";
    private static final long CACHE_TTL = 30; // minutes

    // ============================
    // UserDetailsService Implementation
    // ============================
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        boolean accountLocked = user.getStatus() == UserStatus.LOCKED ||
                (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now()));

        boolean credentialsExpired = user.getPasswordExpiryDate() != null &&
                user.getPasswordExpiryDate().isBefore(LocalDateTime.now());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .accountLocked(accountLocked)
                .credentialsExpired(credentialsExpired)
                .build();
    }

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
        user.setStatus(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE);
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.CUSTOMER);
        user.setPasswordExpiryDate(LocalDateTime.now().plusDays(passwordExpiryDays));
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getUserId());

        UserDTO dto = convertToDTO(savedUser);
        cacheService.put(USER_CACHE, savedUser.getUserId().toString(), dto, CACHE_TTL);
        cacheService.put(USER_CACHE, "email:" + savedUser.getEmail(), dto, CACHE_TTL);

        return dto;
    }

    // ============================
    // GET USER BY ID
    // ============================
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(UUID userId) {
        logger.debug("Fetching user by ID: {}", userId);
        return userRepository.findById(userId).map(this::convertToDTO);
    }

    // ============================
    // GET USER BY EMAIL
    // ============================
    @Cacheable(value = "users", key = "'email:' + #email", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(this::convertToDTO);
    }

    // ============================
    // GET ALL USERS
    // ============================
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll().stream()
                .filter(user -> user.getStatus() != UserStatus.DELETED)
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

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getStatus() != null) user.setStatus(request.getStatus());

        user.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(user);
        UserDTO dto = convertToDTO(updated);

        cacheService.put(USER_CACHE, userId.toString(), dto, CACHE_TTL);
        cacheService.put(USER_CACHE, "email:" + dto.getEmail(), dto, CACHE_TTL);

        return dto;
    }

    // ============================
    // UPDATE STATUS
    // ============================
    public UserDTO updateUserStatus(UUID userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(user);
        return convertToDTO(updated);
    }

    // ============================
    // SOFT DELETE USER
    // ============================
    public void softDeleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.DELETED);
        user.setEmail(user.getEmail() + "_deleted_" + System.currentTimeMillis());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        cacheService.evict(USER_CACHE, userId.toString());
        cacheService.invalidateUserTokens(user.getEmail());
        logger.info("User soft deleted: {}", userId);
    }

    // ============================
    // ACCOUNT LOCK MANAGEMENT
    // ============================
    public void unlockAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        user.setLocked(false);
        user.setLockedUntil(null);
        userRepository.save(user);

        cacheService.clearFailedAttempts(user.getEmail());
        cacheService.evict(USER_CACHE, userId.toString());
        logger.info("Account unlocked for user ID: {}", userId);
    }

    public void lockAccountTemporarily(UUID userId, int minutes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.LOCKED);
        user.setLocked(true);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(minutes));
        userRepository.save(user);

        cacheService.invalidateUserTokens(user.getEmail());
        logger.info("Account temporarily locked for user ID: {}", userId);
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

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordExpiryDate(LocalDateTime.now().plusDays(passwordExpiryDays));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        cacheService.evict(USER_CACHE, userId.toString());
        cacheService.evict(USER_CACHE, "email:" + user.getEmail());
        cacheService.invalidateUserTokens(user.getEmail());
        logger.info("Password changed for user ID: {}", userId);
    }

    // ============================
    // MFA MANAGEMENT
    // ============================
    public void enableMfa(UUID userId, String secret) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        logger.info("MFA enabled for user ID: {}", userId);
    }

    public void disableMfa(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorSecret(null);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
        logger.info("MFA disabled for user ID: {}", userId);
    }

    public boolean isMfaEnabled(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.isTwoFactorEnabled();
    }

    public UUID getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public String getUserEmailById(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getEmail)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    // ============================
    // UTILITY METHODS
    // ============================
    @Transactional(readOnly = true)
    public long getActiveUsersCount() {
        return userRepository.countByStatus(UserStatus.ACTIVE);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setStatus(user.getStatus().name());
        dto.setRole(user.getRole().name());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());

        return dto;
    }
}