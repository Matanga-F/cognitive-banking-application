package com.cognitive.banking.controller;

import com.cognitive.banking.dto.CreateUserRequest;
import com.cognitive.banking.dto.UpdateUserRequest;
import com.cognitive.banking.dto.UserDTO;
import com.cognitive.banking.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ============================
    // CREATE USER
    // ============================
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {

        logger.info("API Request: Create user with email={}", request.getEmail());

        UserDTO user = userService.createUser(request);

        return ResponseEntity
                .status(201)
                .body(user);
    }

    // ============================
    // GET USER BY ID
    // ============================
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID userId) {

        logger.debug("API Request: Get user by ID={}", userId);

        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("User not found: {}", userId);
                    return ResponseEntity.notFound().build();
                });
    }

    // ============================
    // GET USER BY EMAIL
    // ============================
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {

        logger.debug("API Request: Get user by email={}", email);

        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("User not found: {}", email);
                    return ResponseEntity.notFound().build();
                });
    }

    // ============================
    // GET ALL USERS
    // ============================
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {

        logger.info("API Request: Get all users");

        List<UserDTO> users = userService.getAllUsers();

        return ResponseEntity.ok(users);
    }

    // ============================
    // UPDATE USER
    // ============================
    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {

        logger.info("API Request: Update user {}", userId);

        UserDTO updatedUser = userService.updateUser(userId, request);

        return ResponseEntity.ok(updatedUser);
    }

    // ============================
    // UPDATE USER STATUS
    // ============================
    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserDTO> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam String status) {

        logger.info("API Request: Update status {} for user {}", status, userId);

        UserDTO updatedUser = userService.updateUserStatus(userId, status);

        return ResponseEntity.ok(updatedUser);
    }

    // ============================
    // DELETE USER
    // ============================
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {

        logger.warn("API Request: Delete user {}", userId);

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }

    // ============================
    // ACTIVE USERS COUNT
    // ============================
    @GetMapping("/count/active")
    public ResponseEntity<Long> getActiveUsersCount() {

        logger.debug("API Request: Active users count");

        long count = userService.getActiveUsersCount();

        return ResponseEntity.ok(count);
    }

    // ============================
    // HEALTH CHECK
    // ============================
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is healthy");
    }
}


