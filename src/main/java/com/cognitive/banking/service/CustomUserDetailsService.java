package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.UserRole;
import com.cognitive.banking.domain.enums.UserStatus;
import com.cognitive.banking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    // Explicit constructor for dependency injection
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);

        // First try to find active user
        User user = userRepository.findActiveByEmail(email)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email)));

        logger.debug("User found: {} with role: {} and status: {}", user.getEmail(), user.getRole(), user.getStatus());

        // Check if account is locked
        if (user.isLocked()) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                logger.warn("Account locked until: {}", user.getLockedUntil());
                throw new UsernameNotFoundException("Account is temporarily locked. Please try again later.");
            } else if (user.getLockedUntil() == null) {
                logger.warn("Account is permanently locked");
                throw new UsernameNotFoundException("Account is permanently locked. Please contact administrator.");
            }
        }

        // Check if account is expired
        if (user.getAccountExpiryDate() != null && user.getAccountExpiryDate().isBefore(LocalDateTime.now())) {
            logger.warn("Account expired on: {}", user.getAccountExpiryDate());
            throw new UsernameNotFoundException("Account has expired. Please contact administrator.");
        }

        // Check if password is expired
        boolean passwordExpired = user.getPasswordExpiryDate() != null &&
                user.getPasswordExpiryDate().isBefore(LocalDateTime.now());

        // Check if email is verified (optional - can be configured to allow unverified emails with restrictions)
        boolean emailVerified = user.isEmailVerified();

        return new CustomUserDetails(user, passwordExpired, emailVerified);
    }

    /**
     * Load user by user ID (for internal use)
     */
    public UserDetails loadUserById(java.util.UUID userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        return new CustomUserDetails(user, false, user.isEmailVerified());
    }

    /**
     * Custom UserDetails implementation with additional user information
     */
    public static class CustomUserDetails implements UserDetails {

        private final User user;
        private final boolean passwordExpired;
        private final boolean emailVerified;

        public CustomUserDetails(User user, boolean passwordExpired, boolean emailVerified) {
            this.user = user;
            this.passwordExpired = passwordExpired;
            this.emailVerified = emailVerified;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = new ArrayList<>();

            // Add role as authority (prefix with ROLE_ for Spring Security)
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

            // Add permission-based authorities
            Set<String> privileges = getPrivilegesForRole(user.getRole());
            for (String privilege : privileges) {
                authorities.add(new SimpleGrantedAuthority(privilege));
            }

            // Add additional authorities based on user status
            if (emailVerified) {
                authorities.add(new SimpleGrantedAuthority("EMAIL_VERIFIED"));
            }

            if (!passwordExpired) {
                authorities.add(new SimpleGrantedAuthority("PASSWORD_VALID"));
            }

            // Add status-based authorities
            if (user.getStatus() == UserStatus.ACTIVE) {
                authorities.add(new SimpleGrantedAuthority("ACCOUNT_ACTIVE"));
            }

            return authorities;
        }

        /**
         * Get privileges based on user role
         */
        private Set<String> getPrivilegesForRole(UserRole role) {
            Set<String> privileges = new HashSet<>();

            switch (role) {
                case CUSTOMER:
                    privileges.addAll(Set.of(
                            "user:read", "account:read", "transaction:create",
                            "transaction:read", "card:read", "loan:read"
                    ));
                    break;
                case ACCOUNT_MANAGER:
                    privileges.addAll(Set.of(
                            "user:read", "user:update", "account:create", "account:read",
                            "account:update", "account:freeze", "account:unfreeze",
                            "transaction:read", "transaction:approve", "card:create",
                            "card:read", "card:update", "card:block", "card:unblock",
                            "loan:create", "loan:read"
                    ));
                    break;
                case LOAN_OFFICER:
                    privileges.addAll(Set.of(
                            "user:read", "loan:create", "loan:read",
                            "loan:approve", "loan:reject"
                    ));
                    break;
                case ADMIN:
                    privileges.addAll(Set.of(
                            "user:create", "user:read", "user:update", "user:delete",
                            "user:activate", "user:deactivate", "account:create",
                            "account:read", "account:update", "account:delete",
                            "transaction:create", "transaction:read", "transaction:approve",
                            "transaction:reverse", "card:create", "card:read",
                            "card:update", "card:block", "loan:create", "loan:read",
                            "loan:approve", "admin:access", "audit:read",
                            "metrics:read", "system:config"
                    ));
                    break;
                case AUDITOR:
                    privileges.addAll(Set.of(
                            "user:read", "account:read", "transaction:read",
                            "card:read", "loan:read", "audit:read", "metrics:read"
                    ));
                    break;
                default:
                    privileges.add("user:read");
                    break;
            }

            return privileges;
        }

        /**
         * Check if user has a specific permission
         */
        public boolean hasPermission(String permission) {
            return getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(permission));
        }

        /**
         * Check if user has any of the specified permissions
         */
        public boolean hasAnyPermission(String... permissions) {
            Set<String> userPermissions = getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            return Arrays.stream(permissions).anyMatch(userPermissions::contains);
        }

        /**
         * Check if user has all specified permissions
         */
        public boolean hasAllPermissions(String... permissions) {
            Set<String> userPermissions = getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            return Arrays.stream(permissions).allMatch(userPermissions::contains);
        }

        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            // Check if account expiry date is not set or is in the future
            return user.getAccountExpiryDate() == null ||
                    user.getAccountExpiryDate().isAfter(LocalDateTime.now());
        }

        @Override
        public boolean isAccountNonLocked() {
            // Check if account is not locked, or if temporarily locked but lock period has expired
            if (!user.isLocked()) {
                return true;
            }

            if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(LocalDateTime.now())) {
                // Lock period expired, account should be automatically unlocked
                return true;
            }

            return false;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            // Check if password has expired
            return !passwordExpired;
        }

        @Override
        public boolean isEnabled() {
            // Account is enabled if status is ACTIVE
            return user.getStatus() == UserStatus.ACTIVE;
        }

        // Additional methods to access user information
        public User getUser() {
            return user;
        }

        public boolean isEmailVerified() {
            return emailVerified;
        }

        public boolean isPasswordExpired() {
            return passwordExpired;
        }

        public java.util.UUID getUserId() {
            return user.getUserId();
        }

        public String getFirstName() {
            return user.getFirstName();
        }

        public String getLastName() {
            return user.getLastName();
        }

        public String getPhoneNumber() {
            return user.getPhoneNumber();
        }

        public String getFullName() {
            return user.getFirstName() + " " + user.getLastName();
        }

        public UserRole getRole() {
            return user.getRole();
        }

        public UserStatus getStatus() {
            return user.getStatus();
        }

        public boolean isTwoFactorEnabled() {
            return user.isTwoFactorEnabled();
        }

        public String getEmail() {
            return user.getEmail();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            CustomUserDetails that = (CustomUserDetails) obj;
            return Objects.equals(user.getUserId(), that.user.getUserId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(user.getUserId());
        }

        @Override
        public String toString() {
            return "CustomUserDetails{" +
                    "userId=" + user.getUserId() +
                    ", email='" + user.getEmail() + '\'' +
                    ", role=" + user.getRole() +
                    ", status=" + user.getStatus() +
                    ", emailVerified=" + emailVerified +
                    ", passwordExpired=" + passwordExpired +
                    '}';
        }
    }
}