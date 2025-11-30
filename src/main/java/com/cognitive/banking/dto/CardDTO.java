// src/main/java/com/cognitive/banking/dto/CardDTO.java
package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.CardStatus;
import com.cognitive.banking.domain.enums.CardType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CardDTO {
    private UUID cardId;
    private String cardNumber;
    private String cardHolderName;
    private CardType cardType;
    private CardStatus cardStatus;
    private LocalDate expiryDate;
    private BigDecimal dailyLimit;
    private BigDecimal availableBalance;
    private BigDecimal creditLimit;
    private BigDecimal currentBalance;
    private UUID userId;
    private String userName;
    private UUID accountId;
    private String accountNumber;
    private LocalDate issuedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CardDTO() {}

    public CardDTO(UUID cardId, String cardNumber, String cardHolderName, CardType cardType,
                   CardStatus cardStatus, LocalDate expiryDate, BigDecimal dailyLimit,
                   BigDecimal availableBalance, BigDecimal creditLimit, BigDecimal currentBalance,
                   UUID userId, String userName, UUID accountId, String accountNumber,
                   LocalDate issuedDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.cardId = cardId;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.cardType = cardType;
        this.cardStatus = cardStatus;
        this.expiryDate = expiryDate;
        this.dailyLimit = dailyLimit;
        this.availableBalance = availableBalance;
        this.creditLimit = creditLimit;
        this.currentBalance = currentBalance;
        this.userId = userId;
        this.userName = userName;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.issuedDate = issuedDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public LocalDate getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}