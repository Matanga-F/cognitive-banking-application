// src/main/java/com/cognitive/banking/dto/UpdateAccountRequest.java
package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.AccountStatus;

import java.math.BigDecimal;

public class UpdateAccountRequest {
    private BigDecimal overdraftLimit;
    private BigDecimal interestRate;
    private AccountStatus accountStatus;

    // Getters and Setters
    public BigDecimal getOverdraftLimit() { return overdraftLimit; }
    public void setOverdraftLimit(BigDecimal overdraftLimit) { this.overdraftLimit = overdraftLimit; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }
}