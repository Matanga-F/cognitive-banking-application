package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.AccountStatus;
import com.cognitive.banking.domain.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AccountDTO {

    private UUID accountId;
    private String accountNumber;
    private String fullAccountNumber;
    private String routingNumber;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private BigDecimal balance;
    private String currency;
    private BigDecimal overdraftLimit;
    private BigDecimal interestRate;
    private UUID userId;
    private String userName;
    private String userEmail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public AccountDTO() {}

    public AccountDTO(UUID accountId, String accountNumber, String routingNumber,
                      AccountType accountType, AccountStatus accountStatus,
                      BigDecimal balance, String currency, BigDecimal overdraftLimit,
                      BigDecimal interestRate, UUID userId, String userName,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
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

    public AccountDTO(UUID accountId, String accountNumber, String routingNumber,
                      AccountType accountType, AccountStatus accountStatus,
                      BigDecimal balance, String currency, BigDecimal overdraftLimit,
                      BigDecimal interestRate, UUID userId, String userName,
                      String userEmail, LocalDateTime createdAt, LocalDateTime updatedAt) {
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
        this.userEmail = userEmail;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getFullAccountNumber() {
        return fullAccountNumber;
    }

    public void setFullAccountNumber(String fullAccountNumber) {
        this.fullAccountNumber = fullAccountNumber;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Builder pattern for easier object creation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID accountId;
        private String accountNumber;
        private String fullAccountNumber;
        private String routingNumber;
        private AccountType accountType;
        private AccountStatus accountStatus;
        private BigDecimal balance;
        private String currency;
        private BigDecimal overdraftLimit;
        private BigDecimal interestRate;
        private UUID userId;
        private String userName;
        private String userEmail;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder accountId(UUID accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder accountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder fullAccountNumber(String fullAccountNumber) {
            this.fullAccountNumber = fullAccountNumber;
            return this;
        }

        public Builder routingNumber(String routingNumber) {
            this.routingNumber = routingNumber;
            return this;
        }

        public Builder accountType(AccountType accountType) {
            this.accountType = accountType;
            return this;
        }

        public Builder accountStatus(AccountStatus accountStatus) {
            this.accountStatus = accountStatus;
            return this;
        }

        public Builder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder overdraftLimit(BigDecimal overdraftLimit) {
            this.overdraftLimit = overdraftLimit;
            return this;
        }

        public Builder interestRate(BigDecimal interestRate) {
            this.interestRate = interestRate;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AccountDTO build() {
            AccountDTO dto = new AccountDTO();
            dto.setAccountId(accountId);
            dto.setAccountNumber(accountNumber);
            dto.setFullAccountNumber(fullAccountNumber);
            dto.setRoutingNumber(routingNumber);
            dto.setAccountType(accountType);
            dto.setAccountStatus(accountStatus);
            dto.setBalance(balance);
            dto.setCurrency(currency);
            dto.setOverdraftLimit(overdraftLimit);
            dto.setInterestRate(interestRate);
            dto.setUserId(userId);
            dto.setUserName(userName);
            dto.setUserEmail(userEmail);
            dto.setCreatedAt(createdAt);
            dto.setUpdatedAt(updatedAt);
            return dto;
        }
    }

    // toString method for logging
    @Override
    public String toString() {
        return "AccountDTO{" +
                "accountId=" + accountId +
                ", accountNumber='" + maskAccountNumber(accountNumber) + '\'' +
                ", accountType=" + accountType +
                ", accountStatus=" + accountStatus +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
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
        AccountDTO that = (AccountDTO) o;
        return accountId != null && accountId.equals(that.accountId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}