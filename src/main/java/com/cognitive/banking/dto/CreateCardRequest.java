// src/main/java/com/cognitive/banking/dto/CreateCardRequest.java
package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.CardType;
import jakarta.validation.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateCardRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Account ID is required")
    private UUID accountId;

    @NotNull(message = "Card type is required")
    private CardType cardType;

    @NotNull(message = "Card holder name is required")
    private String cardHolderName;

    private BigDecimal dailyLimit;
    private BigDecimal creditLimit;

    // Constructors
    public CreateCardRequest() {}

    public CreateCardRequest(UUID userId, UUID accountId, CardType cardType, String cardHolderName) {
        this.userId = userId;
        this.accountId = accountId;
        this.cardType = cardType;
        this.cardHolderName = cardHolderName;
    }

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public CardType getCardType() { return cardType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
}