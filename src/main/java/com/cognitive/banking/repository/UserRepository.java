package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Primary authentication method (using email)
    Optional<User> findByEmail(String email);
    
    // Alternative login methods
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    // Existence checks
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    
    // Count active users (status is a String in your entity)
    long countByStatus(String status);
}
