package com.cognitive.banking.domain.entity;

import com.cognitive.banking.domain.enums.CardNetwork;
import com.cognitive.banking.domain.enums.CardStatus;
import com.cognitive.banking.domain.enums.CardType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "card_id", updatable = false, nullable = false)
    private UUID cardId;

    @Column(name = "card_number", unique = true, nullable = false, length = 20)
    private String cardNumber;

    @Column(name = "card_holder_name", nullable = false, length = 100)
    private String cardHolderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_network", nullable = false)
    private CardNetwork cardNetwork;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_status", nullable = false)
    private CardStatus cardStatus;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "cvv", nullable = false, length = 255)
    private String cvv;

    @Column(name = "pin", nullable = false, length = 255)
    private String pin;

    @Column(name = "daily_limit", precision = 19, scale = 2)
    private BigDecimal dailyLimit;

    @Column(name = "weekly_limit", precision = 19, scale = 2)
    private BigDecimal weeklyLimit;

    @Column(name = "monthly_limit", precision = 19, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "credit_limit", precision = 19, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "available_balance", precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @Column(name = "current_balance", precision = 19, scale = 2)
    private BigDecimal currentBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Card() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.cardStatus = CardStatus.PENDING_ACTIVATION;
        this.availableBalance = BigDecimal.ZERO;
        this.currentBalance = BigDecimal.ZERO;
        this.issuedDate = LocalDate.now();
        this.dailyLimit = BigDecimal.valueOf(1000.00);
        this.cardNetwork = CardNetwork.VISA;
    }

    public Card(String cardNumber, String cardHolderName, CardType cardType, CardNetwork cardNetwork,
                LocalDate expiryDate, String cvv, String pin, User user, Account account) {
        this();
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.cardType = cardType;
        this.cardNetwork = cardNetwork;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.pin = pin;
        this.user = user;
        this.account = account;
    }

    // Getters and Setters
    public UUID getCardId() {
        return cardId;
    }

    public void setCardId(UUID cardId) {
        this.cardId = cardId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public CardNetwork getCardNetwork() {
        return cardNetwork;
    }

    public void setCardNetwork(CardNetwork cardNetwork) {
        this.cardNetwork = cardNetwork;
    }

    public CardStatus getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(CardStatus cardStatus) {
        this.cardStatus = cardStatus;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal getWeeklyLimit() {
        return weeklyLimit;
    }

    public void setWeeklyLimit(BigDecimal weeklyLimit) {
        this.weeklyLimit = weeklyLimit;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDate issuedDate) {
        this.issuedDate = issuedDate;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
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

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper Methods
    public boolean isExpired() {
        return this.expiryDate != null && this.expiryDate.isBefore(LocalDate.now());
    }

    public boolean isActive() {
        return this.cardStatus == CardStatus.ACTIVE && !isExpired();
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.availableBalance != null && this.availableBalance.compareTo(amount) >= 0;
    }

    public void deductBalance(BigDecimal amount) {
        if (hasSufficientBalance(amount)) {
            this.availableBalance = this.availableBalance.subtract(amount);
            if (this.cardType == CardType.CREDIT) {
                this.currentBalance = this.currentBalance.add(amount);
            }
        } else {
            throw new IllegalStateException("Insufficient balance on card");
        }
    }

    public void addBalance(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.availableBalance = this.availableBalance.add(amount);
            if (this.cardType == CardType.CREDIT && this.currentBalance != null) {
                this.currentBalance = this.currentBalance.subtract(amount).max(BigDecimal.ZERO);
            }
        }
    }

    @Override
    public String toString() {
        return "Card{" +
                "cardId=" + cardId +
                ", cardNumber='" + maskCardNumber() + '\'' +
                ", cardHolderName='" + cardHolderName + '\'' +
                ", cardType=" + cardType +
                ", cardNetwork=" + cardNetwork +
                ", cardStatus=" + cardStatus +
                ", expiryDate=" + expiryDate +
                '}';
    }

    private String maskCardNumber() {
        if (cardNumber == null || cardNumber.length() < 12) {
            return "****";
        }
        return cardNumber.substring(0, 4) + "********" + cardNumber.substring(cardNumber.length() - 4);
    }
}