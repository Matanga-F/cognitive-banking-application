// src/main/java/com/cognitive/banking/repository/UserRepository.java
package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByIdentificationNumber(String identificationNumber);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByIdentificationNumber(String identificationNumber);
    List<User> findByStatus(UserStatus status);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);
}