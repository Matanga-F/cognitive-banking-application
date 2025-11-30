// src/main/java/com/cognitive/banking/dto/CreateTransactionRequest.java
package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateTransactionRequest {
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private String currency;

    private String description;
    private String merchantName;
    private String merchantCategory;

    @NotNull(message = "From account ID is required")
    private UUID fromAccountId;

    private UUID toAccountId;
    private UUID cardId;

    // Constructors
    public CreateTransactionRequest() {}

    public CreateTransactionRequest(TransactionType transactionType, BigDecimal amount,
                                    String currency, UUID fromAccountId) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.fromAccountId = fromAccountId;
    }

    // Getters and Setters
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

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

    public UUID getToAccountId() { return toAccountId; }
    public void setToAccountId(UUID toAccountId) { this.toAccountId = toAccountId; }

    public UUID getCardId() { return cardId; }
    public void setCardId(UUID cardId) { this.cardId = cardId; }
}