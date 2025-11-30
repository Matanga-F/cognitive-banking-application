// src/main/java/com/cognitive/banking/dto/CreateAccountRequest.java
package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.AccountType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public class CreateAccountRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Currency is required")
    private String currency;

    @Positive(message = "Initial deposit must be positive")
    private BigDecimal initialDeposit;

    private BigDecimal overdraftLimit;
    private BigDecimal interestRate;

    // Constructors
    public CreateAccountRequest() {}

    public CreateAccountRequest(UUID userId, AccountType accountType, String currency,
                                BigDecimal initialDeposit) {
        this.userId = userId;
        this.accountType = accountType;
        this.currency = currency;
        this.initialDeposit = initialDeposit;
    }

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; }

    public BigDecimal getOverdraftLimit() { return overdraftLimit; }
    public void setOverdraftLimit(BigDecimal overdraftLimit) { this.overdraftLimit = overdraftLimit; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
}