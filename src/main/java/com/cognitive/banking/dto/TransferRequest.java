package com.cognitive.banking.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class TransferRequest {

    @NotNull(message = "From account ID is required")
    private UUID fromAccountId;

    @NotNull(message = "To account ID is required")
    private UUID toAccountId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private String currency;

    private String description;

    // Constructors
    public TransferRequest() {}

    public TransferRequest(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String currency, String description) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    // Getters and Setters
    public UUID getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(UUID fromAccountId) { this.fromAccountId = fromAccountId; }

    public UUID getToAccountId() { return toAccountId; }
    public void setToAccountId(UUID toAccountId) { this.toAccountId = toAccountId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}