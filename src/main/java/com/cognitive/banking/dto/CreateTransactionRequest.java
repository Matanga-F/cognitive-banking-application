package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class CreateTransactionRequest {

    @NotNull(message = "From account ID is required")
    private UUID fromAccountId;

    private UUID toAccountId;
    private UUID cardId;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private String currency;

    private String description;
    private String merchantName;
    private String merchantCategory;

    // Constructors
    public CreateTransactionRequest() {}

    public CreateTransactionRequest(UUID fromAccountId, UUID toAccountId, UUID cardId,
                                    TransactionType transactionType, BigDecimal amount,
                                    String currency, String description,
                                    String merchantName, String merchantCategory) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.cardId = cardId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.merchantName = merchantName;
        this.merchantCategory = merchantCategory;
    }

    // Getters and Setters
    public UUID getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(UUID fromAccountId) { this.fromAccountId = fromAccountId; }

    public UUID getToAccountId() { return toAccountId; }
    public void setToAccountId(UUID toAccountId) { this.toAccountId = toAccountId; }

    public UUID getCardId() { return cardId; }
    public void setCardId(UUID cardId) { this.cardId = cardId; }

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
}