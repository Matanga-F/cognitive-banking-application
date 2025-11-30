// src/main/java/com/cognitive/banking/dto/AccountDTO.java
package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.AccountStatus;
import com.cognitive.banking.domain.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AccountDTO {
    private UUID accountId;
    private String accountNumber;
    private String routingNumber;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private BigDecimal balance;
    private String currency;
    private BigDecimal overdraftLimit;
    private BigDecimal interestRate;
    private UUID userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public AccountDTO() {}

    public AccountDTO(UUID accountId, String accountNumber, String routingNumber,
                      AccountType accountType, AccountStatus accountStatus, BigDecimal balance,
                      String currency, BigDecimal overdraftLimit, BigDecimal interestRate,
                      UUID userId, String userName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.routingNumber = routingNumber;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.balance = balance;
        this.currency = currency;
        this.overdraftLimit = overdraftLimit;
        this.interestRate = interestRate;
        this.userId = userId;
        this.userName = userName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getRoutingNumber() { return routingNumber; }
    public void setRoutingNumber(String routingNumber) { this.routingNumber = routingNumber; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getOverdraftLimit() { return overdraftLimit; }
    public void setOverdraftLimit(BigDecimal overdraftLimit) { this.overdraftLimit = overdraftLimit; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}