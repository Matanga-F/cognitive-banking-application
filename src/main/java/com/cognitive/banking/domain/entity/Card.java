// src/main/java/com/cognitive/banking/domain/entity/Card.java
package com.cognitive.banking.domain.entity;

import com.cognitive.banking.domain.enums.CardType;
import com.cognitive.banking.domain.enums.CardStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Data
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Card Details
    @Column(unique = true)
    private String cardNumber;

    private String cardHolderName;

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.PENDING_ACTIVATION;

    // Dates
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private LocalDate activationDate;

    // Limits
    private BigDecimal dailyWithdrawalLimit;
    private BigDecimal dailyPurchaseLimit;

    // Security
    private String pinHash;
    private String cvv;
    private Boolean isContactless = true;
    private Boolean isInternational = false;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (issueDate == null) {
            issueDate = LocalDate.now();
        }
        if (expiryDate == null) {
            expiryDate = LocalDate.now().plusYears(3);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}