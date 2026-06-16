package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.CardNetwork;
import com.cognitive.banking.domain.enums.CardStatus;
import com.cognitive.banking.domain.enums.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class CreateCardRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Account ID is required")
    private UUID accountId;

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    @NotNull(message = "Card type is required")
    private CardType cardType;

    @NotNull(message = "Card network is required")
    private CardNetwork cardNetwork;

    @NotBlank(message = "PIN is required")
    private String pin;

    @NotBlank(message = "CVV is required")
    private String cvv;

    private BigDecimal dailyLimit;
    private BigDecimal weeklyLimit;
    private BigDecimal monthlyLimit;
    private BigDecimal creditLimit;
    private CardStatus cardStatus;

    // Constructors
    public CreateCardRequest() {}

    public CreateCardRequest(UUID userId, UUID accountId, String cardHolderName,
                             CardType cardType, CardNetwork cardNetwork, String pin, String cvv,
                             BigDecimal dailyLimit, BigDecimal weeklyLimit,
                             BigDecimal monthlyLimit, BigDecimal creditLimit, CardStatus cardStatus) {
        this.userId = userId;
        this.accountId = accountId;
        this.cardHolderName = cardHolderName;
        this.cardType = cardType;
        this.cardNetwork = cardNetwork;
        this.pin = pin;
        this.cvv = cvv;
        this.dailyLimit = dailyLimit;
        this.weeklyLimit = weeklyLimit;
        this.monthlyLimit = monthlyLimit;
        this.creditLimit = creditLimit;
        this.cardStatus = cardStatus;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public CardNetwork getCardNetwork() {
        return cardNetwork;
    }

    public void setCardNetwork(CardNetwork cardNetwork) {
        this.cardNetwork = cardNetwork;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal getWeeklyLimit() {
        return weeklyLimit;
    }

    public void setWeeklyLimit(BigDecimal weeklyLimit) {
        this.weeklyLimit = weeklyLimit;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public CardStatus getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(CardStatus cardStatus) {
        this.cardStatus = cardStatus;
    }
}