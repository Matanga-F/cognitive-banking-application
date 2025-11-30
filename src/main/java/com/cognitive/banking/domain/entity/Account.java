// src/main/java/com/cognitive/banking/domain/entity/Account.java
package com.cognitive.banking.domain.entity;

import com.cognitive.banking.domain.enums.AccountStatus;
import com.cognitive.banking.domain.enums.AccountType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue
    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "routing_number", nullable = false)
    private String routingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal balance;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "overdraft_limit", precision = 15, scale = 2)
    private BigDecimal overdraftLimit;

    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Account() {
        this.createdAt = LocalDateTime.now();
        this.accountStatus = AccountStatus.ACTIVE;
        this.balance = BigDecimal.ZERO;
    }

    public Account(String accountNumber, String routingNumber, AccountType accountType,
                   String currency, User user) {
        this();
        this.accountNumber = accountNumber;
        this.routingNumber = routingNumber;
        this.accountType = accountType;
        this.currency = currency;
        this.user = user;
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

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}