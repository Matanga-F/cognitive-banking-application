// src/main/java/com/cognitive/banking/dto/TransactionDTO.java
package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.TransactionStatus;
import com.cognitive.banking.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionDTO {
    private UUID transactionId;
    private String transactionReference;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String merchantName;
    private String merchantCategory;
    private UUID fromAccountId;
    private String fromAccountNumber;
    private UUID toAccountId;
    private String toAccountNumber;
    private UUID cardId;
    private String cardNumber;
    private BigDecimal balanceAfter;
    private BigDecimal availableBalanceAfter;
    private LocalDateTime transactionDate;
    private LocalDateTime postedDate;
    private LocalDateTime createdAt;

    // Constructors
    public TransactionDTO() {}

    public TransactionDTO(UUID transactionId, String transactionReference, TransactionType transactionType,
                          TransactionStatus transactionStatus, BigDecimal amount, String currency,
                          String description, String merchantName, String merchantCategory,
                          UUID fromAccountId, String fromAccountNumber, UUID toAccountId,
                          String toAccountNumber, UUID cardId, String cardNumber,
                          BigDecimal balanceAfter, BigDecimal availableBalanceAfter,
                          LocalDateTime transactionDate, LocalDateTime postedDate, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.merchantName = merchantName;
        this.merchantCategory = merchantCategory;
        this.fromAccountId = fromAccountId;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountId = toAccountId;
        this.toAccountNumber = toAccountNumber;
        this.cardId = cardId;
        this.cardNumber = cardNumber;
        this.balanceAfter = balanceAfter;
        this.availableBalanceAfter = availableBalanceAfter;
        this.transactionDate = transactionDate;
        this.postedDate = postedDate;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public TransactionStatus getTransactionStatus() { return transactionStatus; }
    public void setTransactionStatus(TransactionStatus transactionStatus) { this.transactionStatus = transactionStatus; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public String getMerchantCategory() { return merchantCategory; }
    public void setMerchantCategory(String merchantCategory) { this.merchantCategory = merchantCategory; }

    public UUID getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(UUID fromAccountId) { this.fromAccountId = fromAccountId; }

    public String getFromAccountNumber() { return fromAccountNumber; }
    public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }

    public UUID getToAccountId() { return toAccountId; }
    public void setToAccountId(UUID toAccountId) { this.toAccountId = toAccountId; }

    public String getToAccountNumber() { return toAccountNumber; }
    public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }

    public UUID getCardId() { return cardId; }
    public void setCardId(UUID cardId) { this.cardId = cardId; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public BigDecimal getAvailableBalanceAfter() { return availableBalanceAfter; }
    public void setAvailableBalanceAfter(BigDecimal availableBalanceAfter) { this.availableBalanceAfter = availableBalanceAfter; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public LocalDateTime getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDateTime postedDate) { this.postedDate = postedDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}