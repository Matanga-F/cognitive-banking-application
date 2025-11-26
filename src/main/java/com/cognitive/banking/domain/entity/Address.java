// src/main/java/com/cognitive/banking/domain/entity/Address.java
package com.cognitive.banking.domain.entity;

import com.cognitive.banking.domain.enums.AddressType;
import com.cognitive.banking.domain.enums.Country;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private AddressType addressType;

    private String streetAddress;
    private String city;
    private String stateProvince;
    private String postalCode;

    @Enumerated(EnumType.STRING)
    private Country country;

    private Boolean isPrimary = false;
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