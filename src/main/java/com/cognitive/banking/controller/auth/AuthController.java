// Update your AuthController.java with security annotations
package com.cognitive.banking.controller.auth;

import com.cognitive.banking.annotation.RequiresPermission;
import com.cognitive.banking.annotation.RequiresRole;
import com.cognitive.banking.domain.enums.UserRole;
import com.cognitive.banking.dto.*;
import com.cognitive.banking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Public endpoints - no security needed
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email/{token}")
    public ResponseEntity<Void> verifyEmail(@PathVariable String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    // Authenticated endpoints - require authentication
    @PostMapping("/logout")
    @RequiresPermission("user:read")  // Any authenticated user can logout
    public ResponseEntity<Void> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            authService.logout(auth.getName());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @RequiresPermission("user:update")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        authService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-verification-email")
    @RequiresPermission("user:update")
    public ResponseEntity<Void> sendVerificationEmail(Authentication authentication) {
        String email = authentication.getName();
        authService.sendVerificationEmail(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/enable-2fa")
    @RequiresPermission("user:update")
    public ResponseEntity<String> enableTwoFactorAuth(Authentication authentication) {
        String email = authentication.getName();
        String secret = authService.enableTwoFactorAuth(email);
        return ResponseEntity.ok(secret);
    }

    @PostMapping("/disable-2fa")
    @RequiresPermission("user:update")
    public ResponseEntity<Void> disableTwoFactorAuth(
            @RequestParam String code,
            Authentication authentication
    ) {
        String email = authentication.getName();
        authService.disableTwoFactorAuth(email, code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-phone")
    @RequiresPermission("user:update")
    public ResponseEntity<Void> verifyPhone(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyPhoneNumber(request.getPhoneNumber(), request.getOtp());
        return ResponseEntity.ok().build();
    }

    // Admin-only endpoints
    @GetMapping("/users")
    @RequiresRole(UserRole.ADMIN)
    @RequiresPermission("user:read")
    public ResponseEntity<?> getAllUsers() {
        // Return all users - you'll need to inject UserService
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}")
    @RequiresRole(UserRole.ADMIN)
    @RequiresPermission("user:delete")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        // Delete user logic
        return ResponseEntity.ok().build();
    }
}