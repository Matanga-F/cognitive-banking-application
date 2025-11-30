// src/main/java/com/cognitive/banking/domain/entity/Card.java
package com.cognitive.banking.domain.entity;

import com.cognitive.banking.domain.enums.CardStatus;
import com.cognitive.banking.domain.enums.CardType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue
    @Column(name = "card_id")
    private UUID cardId;

    @Column(name = "card_number", unique = true, nullable = false)
    private String cardNumber;

    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_status", nullable = false)
    private CardStatus cardStatus;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "cvv", nullable = false)
    private String cvv;

    @Column(name = "daily_limit", precision = 15, scale = 2)
    private BigDecimal dailyLimit;

    @Column(name = "available_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal availableBalance;

    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "current_balance", precision = 15, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "pin", nullable = false)
    private String pin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Card() {
        this.createdAt = LocalDateTime.now();
        this.cardStatus = CardStatus.ACTIVE;
        this.availableBalance = BigDecimal.ZERO;
        this.currentBalance = BigDecimal.ZERO;
        this.issuedDate = LocalDate.now();
    }

    public Card(String cardNumber, String cardHolderName, CardType cardType,
                LocalDate expiryDate, String cvv, String pin, User user, Account account) {
        this();
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.cardType = cardType;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.pin = pin;
        this.user = user;
        this.account = account;
        this.issuedDate = LocalDate.now();
    }

    // Getters and Setters
    public UUID getCardId() { return cardId; }
    public void setCardId(UUID cardId) { this.cardId = cardId; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public CardType getCardType() { return cardType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }

    public CardStatus getCardStatus() { return cardStatus; }
    public void setCardStatus(CardStatus cardStatus) { this.cardStatus = cardStatus; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public LocalDate getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}