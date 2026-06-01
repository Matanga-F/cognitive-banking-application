package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Roles must be prefixed with "ROLE_" for Spring Security
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        // Account lock logic (temporary lock from brute force or permanent from status)
        boolean accountLocked = !user.isAccountNonLocked();

        // Credential expiry (optional but recommended for banking)
        boolean credentialsExpired = user.getPasswordExpiryDate() != null &&
                user.getPasswordExpiryDate().isBefore(LocalDateTime.now());

        // Account expiry (e.g., dormant account)
        boolean accountExpired = user.getAccountExpiryDate() != null &&
                user.getAccountExpiryDate().isBefore(LocalDateTime.now());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(authority))
                .accountLocked(accountLocked)
                .accountExpired(accountExpired)
                .credentialsExpired(credentialsExpired)
                .disabled(false)   // we don't use this field
                .build();
    }
}