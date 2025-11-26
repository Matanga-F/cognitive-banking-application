// src/main/java/com/cognitive/banking/service/UserService.java
package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.UserRole;
import com.cognitive.banking.domain.enums.UserStatus;
import com.cognitive.banking.dto.CreateUserRequest;
import com.cognitive.banking.dto.UserDTO;
import com.cognitive.banking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO createUser(CreateUserRequest request) {
        // Validate unique constraints
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        if (userRepository.existsByIdentificationNumber(request.getIdentificationNumber())) {
            throw new RuntimeException("Identification number already exists: " + request.getIdentificationNumber());
        }

        // Create user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(request.getPassword()); // In real app, encrypt this
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setMaritalStatus(request.getMaritalStatus());
        user.setEmploymentStatus(request.getEmploymentStatus());
        user.setOccupation(request.getOccupation());
        user.setMonthlyIncome(request.getMonthlyIncome());
        user.setIdentificationType(request.getIdentificationType());
        user.setIdentificationNumber(request.getIdentificationNumber());
        user.setStatus(UserStatus.ACTIVE);
        user.getRoles().add(UserRole.CUSTOMER);

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return convertToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setStatus(status);
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setGender(user.getGender());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setMaritalStatus(user.getMaritalStatus());
        dto.setEmploymentStatus(user.getEmploymentStatus());
        dto.setOccupation(user.getOccupation());
        dto.setMonthlyIncome(user.getMonthlyIncome());
        dto.setIdentificationType(user.getIdentificationType());
        dto.setIdentificationNumber(user.getIdentificationNumber());
        dto.setStatus(user.getStatus());
        dto.setRoles(user.getRoles());
        dto.setKycStatus(user.getKycStatus());
        dto.setRiskLevel(user.getRiskLevel());
        dto.setAccountTier(user.getAccountTier());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}