// src/main/java/com/cognitive/banking/domain/entity/Transaction.java
package com.cognitive.banking.domain.entity;

import com.cognitive.banking.domain.enums.TransactionStatus;
import com.cognitive.banking.domain.enums.TransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue
    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "transaction_reference", unique = true, nullable = false)
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false)
    private TransactionStatus transactionStatus;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "description")
    private String description;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "merchant_category")
    private String merchantCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "available_balance_after", precision = 15, scale = 2)
    private BigDecimal availableBalanceAfter;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "posted_date")
    private LocalDateTime postedDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Transaction() {
        this.createdAt = LocalDateTime.now();
        this.transactionStatus = TransactionStatus.PENDING;
        this.transactionDate = LocalDateTime.now();
    }

    public Transaction(String transactionReference, TransactionType transactionType,
                       BigDecimal amount, String currency, Account fromAccount) {
        this();
        this.transactionReference = transactionReference;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.fromAccount = fromAccount;
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

    public Account getFromAccount() { return fromAccount; }
    public void setFromAccount(Account fromAccount) { this.fromAccount = fromAccount; }

    public Account getToAccount() { return toAccount; }
    public void setToAccount(Account toAccount) { this.toAccount = toAccount; }

    public Card getCard() { return card; }
    public void setCard(Card card) { this.card = card; }

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