package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.dto.CreateUserRequest;
import com.cognitive.banking.dto.UpdateUserRequest;
import com.cognitive.banking.dto.UserDTO;
import com.cognitive.banking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String USER_CACHE = "users";
    private static final long CACHE_TTL = 30; // minutes

    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "users", key = "'email:' + #request.email")
    })
    public UserDTO createUser(CreateUserRequest request) {
        logger.info("Creating new user with email: {}", request.getEmail());

        // Validate unique constraints
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("User creation failed - email already exists: {}", request.getEmail());
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            logger.warn("User creation failed - phone number already exists: {}", request.getPhoneNumber());
            throw new RuntimeException("User with phone number " + request.getPhoneNumber() + " already exists");
        }

        // Create user entity
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        // Encrypt password
        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encryptedPassword);

        user.setStatus("ACTIVE");
        user.setRole("CUSTOMER");

        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getUserId());

        // Cache the new user
        cacheService.put(USER_CACHE, savedUser.getUserId().toString(), convertToDTO(savedUser), CACHE_TTL);
        cacheService.put(USER_CACHE, "email:" + savedUser.getEmail(), convertToDTO(savedUser), CACHE_TTL);

        return convertToDTO(savedUser);
    }

    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(UUID userId) {
        logger.debug("Fetching user by ID from database: {}", userId);

        // First check cache manually for better control
        UserDTO cachedUser = (UserDTO) cacheService.get(USER_CACHE, userId.toString());
        if (cachedUser != null) {
            logger.debug("User cache hit for ID: {}", userId);
            return Optional.of(cachedUser);
        }

        logger.info("User cache miss for ID: {}, fetching from database", userId);
        Optional<UserDTO> userDTO = userRepository.findById(userId)
                .map(this::convertToDTO);

        // Cache the result
        userDTO.ifPresent(dto ->
                cacheService.put(USER_CACHE, userId.toString(), dto, CACHE_TTL)
        );

        return userDTO;
    }

    @Cacheable(value = "users", key = "'email:' + #email", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        logger.debug("Fetching user by email from database: {}", email);

        // First check cache manually
        UserDTO cachedUser = (UserDTO) cacheService.get(USER_CACHE, "email:" + email);
        if (cachedUser != null) {
            logger.debug("User cache hit for email: {}", email);
            return Optional.of(cachedUser);
        }

        logger.info("User cache miss for email: {}, fetching from database", email);
        Optional<UserDTO> userDTO = userRepository.findByEmail(email)
                .map(this::convertToDTO);

        // Cache the result
        userDTO.ifPresent(dto ->
                cacheService.put(USER_CACHE, "email:" + email, dto, CACHE_TTL)
        );

        return userDTO;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        logger.info("Fetching all users from database");

        // For lists, we don't cache the entire list but individual users
        List<UserDTO> users = userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Cache each user individually
        users.forEach(user ->
                cacheService.put(USER_CACHE, user.getUserId().toString(), user, CACHE_TTL)
        );

        return users;
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "users", key = "'email:' + #result.email", condition = "#result != null")
    })
    public UserDTO updateUser(UUID userId, UpdateUserRequest request) {
        logger.info("Updating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new RuntimeException("User not found with ID: " + userId);
                });

        String oldEmail = user.getEmail();
        boolean emailChanged = false;

        // Update fields if provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                logger.warn("Email update failed - email already exists: {}", request.getEmail());
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
            emailChanged = true;
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                logger.warn("Phone number update failed - already exists: {}", request.getPhoneNumber());
                throw new RuntimeException("Phone number already exists: " + request.getPhoneNumber());
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully: {}", userId);

        // Update cache
        cacheService.put(USER_CACHE, userId.toString(), convertToDTO(updatedUser), CACHE_TTL);
        cacheService.put(USER_CACHE, "email:" + updatedUser.getEmail(), convertToDTO(updatedUser), CACHE_TTL);

        // Remove old email from cache if email changed
        if (emailChanged && !oldEmail.equals(updatedUser.getEmail())) {
            cacheService.evict(USER_CACHE, "email:" + oldEmail);
        }

        return convertToDTO(updatedUser);
    }

    @CacheEvict(value = "users", key = "#userId")
    public UserDTO updateUserStatus(UUID userId, String status) {
        logger.info("Updating user status to {} for user ID: {}", status, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new RuntimeException("User not found with ID: " + userId);
                });

        user.setStatus(status);
        User updatedUser = userRepository.save(user);

        logger.info("User status updated successfully: {}", userId);

        // Update cache
        cacheService.put(USER_CACHE, userId.toString(), convertToDTO(updatedUser), CACHE_TTL);
        cacheService.put(USER_CACHE, "email:" + updatedUser.getEmail(), convertToDTO(updatedUser), CACHE_TTL);

        return convertToDTO(updatedUser);
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "users", key = "'email:' + #result", condition = "#result != null")
    })
    public void deleteUser(UUID userId) {
        logger.info("Deleting user with ID: {}", userId);

        // Get user email before deletion for cache cleanup
        String userEmail = userRepository.findById(userId)
                .map(User::getEmail)
                .orElse(null);

        if (!userRepository.existsById(userId)) {
            logger.error("User not found with ID: {}", userId);
            throw new RuntimeException("User not found with ID: " + userId);
        }

        userRepository.deleteById(userId);

        // Remove from cache
        cacheService.evict(USER_CACHE, userId.toString());
        if (userEmail != null) {
            cacheService.evict(USER_CACHE, "email:" + userEmail);
        }

        logger.info("User deleted successfully: {}", userId);
    }

    @CacheEvict(value = "users", key = "#userId")
    public void updateLastLogin(UUID userId) {
        logger.debug("Updating last login for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new RuntimeException("User not found with ID: " + userId);
                });

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Update cache with new last login time
        getUserById(userId).ifPresent(dto ->
                cacheService.put(USER_CACHE, userId.toString(), dto, CACHE_TTL)
        );
    }

    @Transactional(readOnly = true)
    public long getActiveUsersCount() {
        logger.debug("Fetching active users count");
        return userRepository.countByStatus("ACTIVE");
    }

    // Additional method to clear all user caches (useful for admin operations)
    public void clearUserCaches() {
        logger.info("Clearing all user caches");
        cacheService.evictPattern(USER_CACHE, "*");
    }

    // Additional method to validate user credentials
    @Transactional(readOnly = true)
    public Optional<UserDTO> validateUserCredentials(String email, String password) {
        logger.debug("Validating user credentials for email: {}", email);

        return userRepository.findByEmail(email)
                .map(user -> {
                    if (passwordEncoder.matches(password, user.getPassword())) {
                        return convertToDTO(user);
                    }
                    return null;
                });
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),
                user.getRole(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}