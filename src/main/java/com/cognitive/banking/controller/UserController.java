package com.cognitive.banking.controller;

import com.cognitive.banking.dto.CreateUserRequest;
import com.cognitive.banking.dto.UpdateUserRequest;
import com.cognitive.banking.dto.UserDTO;
import com.cognitive.banking.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    // CREATE USER (Admin only)
    // ============================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        logger.info("API Request: Create user with email={}", request.getEmail());
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(201).body(user);
    }

    // ============================
    // GET USER BY ID (User can read self, Admin can read any)
    // ============================
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
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
    // GET USER BY EMAIL (Admin only – prevents email enumeration)
    // ============================
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
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
    // GET ALL USERS (Admin only)
    // ============================
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("API Request: Get all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ============================
    // UPDATE USER (User can update self, Admin can update any)
    // ============================
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        logger.info("API Request: Update user {}", userId);
        UserDTO updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    // ============================
    // UPDATE USER STATUS (Admin only – can lock/activate)
    // ============================
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam String status) {
        logger.info("API Request: Update status {} for user {}", status, userId);
        UserDTO updatedUser = userService.updateUserStatus(userId, status);
        return ResponseEntity.ok(updatedUser);
    }

    // ============================
    // SOFT DELETE USER (Admin only)
    // ============================
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        logger.warn("API Request: Soft delete user {}", userId);
        userService.softDeleteUser(userId);   // ✅ Changed from deleteUser to softDeleteUser
        return ResponseEntity.noContent().build();
    }

    // ============================
    // ACTIVE USERS COUNT (Admin only – internal metric)
    // ============================
    @GetMapping("/count/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getActiveUsersCount() {
        logger.debug("API Request: Active users count");
        return ResponseEntity.ok(userService.getActiveUsersCount());
    }

    // ============================
    // HEALTH CHECK (public)
    // ============================
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is healthy");
    }
}