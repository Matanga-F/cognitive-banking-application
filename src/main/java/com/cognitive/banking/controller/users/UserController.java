package com.cognitive.banking.controller.users;

import com.cognitive.banking.dto.ChangePasswordRequest;
import com.cognitive.banking.dto.UpdateUserRequest;
import com.cognitive.banking.dto.UserDTO;
import com.cognitive.banking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get current authenticated user's profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(Authentication authentication) {
        String email = authentication.getName();
        UserDTO user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    /**
     * Update current user's profile
     */
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        UserDTO currentUser = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDTO updatedUser = userService.updateUser(currentUser.getUserId(), request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Change password for current user
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        UserDTO user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userService.changePassword(user.getUserId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    /**
     * Delete current user's account (soft delete)
     */
    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        String email = authentication.getName();
        UserDTO user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userService.softDeleteUser(user.getUserId());
        return ResponseEntity.noContent().build();
    }

    // Helper method - replace with actual TOTP generation
    private String generateMfaSecret() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}