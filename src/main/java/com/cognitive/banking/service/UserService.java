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

        // 🔐 HASH PASSWORD
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setStatus(request.getStatus());
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getUserId());

        UserDTO dto = convertToDTO(savedUser);

        cacheService.put(USER_CACHE, savedUser.getUserId().toString(), dto, CACHE_TTL);
        cacheService.put(USER_CACHE, "email:" + savedUser.getEmail(), dto, CACHE_TTL);

        return dto;
    }

    // ============================
    // AUTHENTICATION (NEW CORE)
    // ============================
    @Transactional(readOnly = true)
    public UserDTO authenticate(String email, String password) {
        logger.info("Authenticating user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Authentication failed - user not found: {}", email);
                    return new RuntimeException("Invalid credentials");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Authentication failed - invalid password for email: {}", email);
            throw new RuntimeException("Invalid credentials");
        }

        logger.info("Authentication successful for user ID: {}", user.getUserId());

        // update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return convertToDTO(user);
    }

    // ============================
    // GET USER BY ID
    // ============================
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(UUID userId) {
        logger.debug("Fetching user by ID: {}", userId);

        UserDTO cached = (UserDTO) cacheService.get(USER_CACHE, userId.toString());
        if (cached != null) {
            logger.debug("Cache hit for user ID: {}", userId);
            return Optional.of(cached);
        }

        Optional<UserDTO> userDTO = userRepository.findById(userId)
                .map(this::convertToDTO);

        userDTO.ifPresent(dto ->
                cacheService.put(USER_CACHE, userId.toString(), dto, CACHE_TTL)
        );

        return userDTO;
    }

    // ============================
    // GET USER BY EMAIL
    // ============================
    @Cacheable(value = "users", key = "'email:' + #email", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {

        UserDTO cached = (UserDTO) cacheService.get(USER_CACHE, "email:" + email);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<UserDTO> userDTO = userRepository.findByEmail(email)
                .map(this::convertToDTO);

        userDTO.ifPresent(dto ->
                cacheService.put(USER_CACHE, "email:" + email, dto, CACHE_TTL)
        );

        return userDTO;
    }

    // ============================
    // GET ALL USERS
    // ============================
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        logger.info("Fetching all users");

        return userRepository.findAll()
                .stream()
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

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User updated = userRepository.save(user);

        UserDTO dto = convertToDTO(updated);

        cacheService.put(USER_CACHE, userId.toString(), dto, CACHE_TTL);
        cacheService.put(USER_CACHE, "email:" + dto.getEmail(), dto, CACHE_TTL);

        return dto;
    }

    // ============================
    // UPDATE STATUS
    // ============================
    public UserDTO updateUserStatus(UUID userId, String status) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(status);
        User updated = userRepository.save(user);

        return convertToDTO(updated);
    }

    // ============================
    // DELETE USER
    // ============================
    public void deleteUser(UUID userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        userRepository.deleteById(userId);
        cacheService.evict(USER_CACHE, userId.toString());
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
                user.getStatus(),
                user.getRole(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
    public long getActiveUsersCount() {
	        return userRepository.countByStatus("ACTIVE");
    }
}


