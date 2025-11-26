// src/main/java/com/cognitive/banking/domain/entity/Branch.java
package com.cognitive.banking.domain.entity;

import com.cognitive.banking.domain.enums.BranchType;
import com.cognitive.banking.domain.enums.Country;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "branches")
@Data
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String branchCode;
    private String branchName;

    @Enumerated(EnumType.STRING)
    private BranchType branchType;

    // Address
    private String address;
    private String city;
    private String stateProvince;
    private String postalCode;

    @Enumerated(EnumType.STRING)
    private Country country;

    // Contact
    private String phone;
    private String email;
    private String managerName;

    // Operating Hours
    private LocalTime openingTime;
    private LocalTime closingTime;
    private String workingDays; // "MON-FRI" or "MON-SAT"

    // Coordinates
    private Double latitude;
    private Double longitude;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}