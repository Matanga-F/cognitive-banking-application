// src/main/java/com/cognitive/banking/domain/entity/Account.java
package com.cognitive.banking.domain.entity;

import com.cognitive.banking.domain.enums.AccountStatus;
import com.cognitive.banking.domain.enums.AccountType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_account_number", columnList = "account_number"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_account_status", columnList = "account_status"),
        @Index(name = "idx_account_type", columnList = "account_type"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "account_id", updatable = false, nullable = false)
    private UUID accountId;

    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "routing_number", nullable = false, length = 9)
    private String routingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    private AccountStatus accountStatus;

    @Column(name = "balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal balance;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "overdraft_limit", precision = 15, scale = 2)
    private BigDecimal overdraftLimit;

    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "available_balance", precision = 15, scale = 2)
    private BigDecimal availableBalance;

    @Column(name = "minimum_balance_required", precision = 15, scale = 2)
    private BigDecimal minimumBalanceRequired;

    @Column(name = "maintenance_fee", precision = 15, scale = 2)
    private BigDecimal maintenanceFee;

    @Column(name = "is_overdraft_protection_enabled")
    private boolean overdraftProtectionEnabled;

    @Column(name = "is_frozen")
    private boolean frozen;

    @Column(name = "freeze_reason", length = 255)
    private String freezeReason;

    @Column(name = "frozen_at")
    private LocalDateTime frozenAt;

    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;

    @Column(name = "last_interest_calculation_date")
    private LocalDateTime lastInterestCalculationDate;

    @Column(name = "dormant_notification_sent")
    private boolean dormantNotificationSent;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closed_reason", length = 255)
    private String closedReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    // Constructors
    public Account() {
        this.createdAt = LocalDateTime.now();
        this.accountStatus = AccountStatus.ACTIVE;
        this.balance = BigDecimal.ZERO;
        this.availableBalance = BigDecimal.ZERO;
        this.overdraftLimit = BigDecimal.ZERO;
        this.interestRate = BigDecimal.ZERO;
        this.minimumBalanceRequired = BigDecimal.ZERO;
        this.maintenanceFee = BigDecimal.ZERO;
        this.overdraftProtectionEnabled = false;
        this.frozen = false;
        this.dormantNotificationSent = false;
        this.version = 0L;
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

    // Business methods
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.lastTransactionDate = LocalDateTime.now();
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        BigDecimal available = getAvailableFunds();
        if (available.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        this.balance = this.balance.subtract(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
        this.lastTransactionDate = LocalDateTime.now();
    }

    public BigDecimal getAvailableFunds() {
        if (overdraftProtectionEnabled && overdraftLimit != null) {
            return availableBalance.add(overdraftLimit);
        }
        return availableBalance;
    }

    public boolean isOverdrawn() {
        return balance != null && balance.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isBelowMinimumBalance() {
        return minimumBalanceRequired != null &&
                balance != null &&
                balance.compareTo(minimumBalanceRequired) < 0;
    }

    public boolean isDormant() {
        if (lastTransactionDate == null) {
            return createdAt != null && createdAt.isBefore(LocalDateTime.now().minusMonths(6));
        }
        return lastTransactionDate.isBefore(LocalDateTime.now().minusMonths(6));
    }

    public void freeze(String reason) {
        this.frozen = true;
        this.freezeReason = reason;
        this.frozenAt = LocalDateTime.now();
        this.accountStatus = AccountStatus.FROZEN;
    }

    public void unfreeze() {
        this.frozen = false;
        this.freezeReason = null;
        this.frozenAt = null;
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public void close(String reason) {
        this.accountStatus = AccountStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
        this.closedReason = reason;
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

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

    public BigDecimal getMinimumBalanceRequired() { return minimumBalanceRequired; }
    public void setMinimumBalanceRequired(BigDecimal minimumBalanceRequired) { this.minimumBalanceRequired = minimumBalanceRequired; }

    public BigDecimal getMaintenanceFee() { return maintenanceFee; }
    public void setMaintenanceFee(BigDecimal maintenanceFee) { this.maintenanceFee = maintenanceFee; }

    public boolean isOverdraftProtectionEnabled() { return overdraftProtectionEnabled; }
    public void setOverdraftProtectionEnabled(boolean overdraftProtectionEnabled) { this.overdraftProtectionEnabled = overdraftProtectionEnabled; }

    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }

    public String getFreezeReason() { return freezeReason; }
    public void setFreezeReason(String freezeReason) { this.freezeReason = freezeReason; }

    public LocalDateTime getFrozenAt() { return frozenAt; }
    public void setFrozenAt(LocalDateTime frozenAt) { this.frozenAt = frozenAt; }

    public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }

    public LocalDateTime getLastInterestCalculationDate() { return lastInterestCalculationDate; }
    public void setLastInterestCalculationDate(LocalDateTime lastInterestCalculationDate) { this.lastInterestCalculationDate = lastInterestCalculationDate; }

    public boolean isDormantNotificationSent() { return dormantNotificationSent; }
    public void setDormantNotificationSent(boolean dormantNotificationSent) { this.dormantNotificationSent = dormantNotificationSent; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public String getClosedReason() { return closedReason; }
    public void setClosedReason(String closedReason) { this.closedReason = closedReason; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (availableBalance == null) {
            availableBalance = balance;
        }
        if (overdraftLimit == null) {
            overdraftLimit = BigDecimal.ZERO;
        }
        if (interestRate == null) {
            interestRate = BigDecimal.ZERO;
        }
        if (minimumBalanceRequired == null) {
            minimumBalanceRequired = BigDecimal.ZERO;
        }
        if (maintenanceFee == null) {
            maintenanceFee = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (availableBalance == null) {
            availableBalance = balance;
        }
    }

    // toString method for logging
    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", accountNumber='" + maskAccountNumber(accountNumber) + '\'' +
                ", accountType=" + accountType +
                ", accountStatus=" + accountStatus +
                ", currency='" + currency + '\'' +
                ", userId=" + (user != null ? user.getUserId() : null) +
                '}';
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
            return accountNumber;
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return accountId != null && accountId.equals(account.accountId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}