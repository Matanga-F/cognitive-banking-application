// src/main/java/com/cognitive/banking/dto/UpdateCardRequest.java
package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.CardStatus;

import java.math.BigDecimal;

public class UpdateCardRequest {
    private BigDecimal dailyLimit;
    private BigDecimal creditLimit;
    private CardStatus cardStatus;

    // Getters and Setters
    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public CardStatus getCardStatus() { return cardStatus; }
    public void setCardStatus(CardStatus cardStatus) { this.cardStatus = cardStatus; }
}