// src/main/java/com/cognitive/banking/config/TestDataConfig.java
package com.cognitive.banking.config;

import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.*;
import com.cognitive.banking.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class TestDataConfig {
    private final UserRepository userRepository;

    public TestDataConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void initTestData() {
        if (userRepository.count() == 0) {
            // Create test users
            User user1 = new User();
            user1.setFirstName("John");
            user1.setLastName("Doe");
            user1.setUsername("johndoe");
            user1.setEmail("john@bank.com");
            user1.setPhoneNumber("1234567890");
            user1.setPassword("password123");
            user1.setGender(Gender.MALE);
            user1.setDateOfBirth(LocalDate.of(1985, 5, 15));
            user1.setMaritalStatus(MaritalStatus.MARRIED);
            user1.setEmploymentStatus(EmploymentStatus.EMPLOYED);
            user1.setOccupation("Software Engineer");
            user1.setMonthlyIncome(new BigDecimal("75000"));
            user1.setIdentificationType(IdentificationType.NATIONAL_ID);
            user1.setIdentificationNumber("ID123456789");
            user1.setStatus(UserStatus.ACTIVE);
            user1.getRoles().add(UserRole.CUSTOMER);

            User user2 = new User();
            user2.setFirstName("Jane");
            user2.setLastName("Smith");
            user2.setUsername("janesmith");
            user2.setEmail("jane@bank.com");
            user2.setPhoneNumber("0987654321");
            user2.setPassword("password123");
            user2.setGender(Gender.FEMALE);
            user2.setDateOfBirth(LocalDate.of(1990, 8, 22));
            user2.setMaritalStatus(MaritalStatus.SINGLE);
            user2.setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);
            user2.setOccupation("Business Owner");
            user2.setMonthlyIncome(new BigDecimal("120000"));
            user2.setIdentificationType(IdentificationType.PASSPORT);
            user2.setIdentificationNumber("PASS987654");
            user2.setStatus(UserStatus.ACTIVE);
            user2.getRoles().add(UserRole.CUSTOMER);

            userRepository.save(user1);
            userRepository.save(user2);
        }
    }
}