package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.CardStatus;
import com.cognitive.banking.domain.enums.CardType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CardDTO {

    private UUID cardId;
    private String cardNumber;
    private String fullCardNumber;
    private String cardHolderName;
    private CardType cardType;
    private CardStatus cardStatus;
    private LocalDate expiryDate;
    private BigDecimal dailyLimit;
    private BigDecimal availableBalance;
    private BigDecimal creditLimit;
    private BigDecimal currentBalance;
    private UUID userId;
    private String userName;
    private String userEmail;
    private UUID accountId;
    private String accountNumber;
    private String fullAccountNumber;
    private LocalDate issuedDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public CardDTO() {}

    public CardDTO(UUID cardId, String cardNumber, String fullCardNumber, String cardHolderName,
                   CardType cardType, CardStatus cardStatus, LocalDate expiryDate,
                   BigDecimal dailyLimit, BigDecimal availableBalance, BigDecimal creditLimit,
                   BigDecimal currentBalance, UUID userId, String userName, String userEmail,
                   UUID accountId, String accountNumber, String fullAccountNumber, LocalDate issuedDate,
                   LocalDateTime activatedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.cardId = cardId;
        this.cardNumber = cardNumber;
        this.fullCardNumber = fullCardNumber;
        this.cardHolderName = cardHolderName;
        this.cardType = cardType;
        this.cardStatus = cardStatus;
        this.expiryDate = expiryDate;
        this.dailyLimit = dailyLimit;
        this.availableBalance = availableBalance;
        this.creditLimit = creditLimit;
        this.currentBalance = currentBalance;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.fullAccountNumber = fullAccountNumber;
        this.issuedDate = issuedDate;
        this.activatedAt = activatedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getFullCardNumber() {
        return fullCardNumber;
    }

    public void setFullCardNumber(String fullCardNumber) {
        this.fullCardNumber = fullCardNumber;
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

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
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

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID cardId;
        private String cardNumber;
        private String fullCardNumber;
        private String cardHolderName;
        private CardType cardType;
        private CardStatus cardStatus;
        private LocalDate expiryDate;
        private BigDecimal dailyLimit;
        private BigDecimal availableBalance;
        private BigDecimal creditLimit;
        private BigDecimal currentBalance;
        private UUID userId;
        private String userName;
        private String userEmail;
        private UUID accountId;
        private String accountNumber;
        private String fullAccountNumber;
        private LocalDate issuedDate;
        private LocalDateTime activatedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder cardId(UUID cardId) { this.cardId = cardId; return this; }
        public Builder cardNumber(String cardNumber) { this.cardNumber = cardNumber; return this; }
        public Builder fullCardNumber(String fullCardNumber) { this.fullCardNumber = fullCardNumber; return this; }
        public Builder cardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; return this; }
        public Builder cardType(CardType cardType) { this.cardType = cardType; return this; }
        public Builder cardStatus(CardStatus cardStatus) { this.cardStatus = cardStatus; return this; }
        public Builder expiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder dailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; return this; }
        public Builder availableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; return this; }
        public Builder creditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; return this; }
        public Builder currentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder userName(String userName) { this.userName = userName; return this; }
        public Builder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public Builder accountId(UUID accountId) { this.accountId = accountId; return this; }
        public Builder accountNumber(String accountNumber) { this.accountNumber = accountNumber; return this; }
        public Builder fullAccountNumber(String fullAccountNumber) { this.fullAccountNumber = fullAccountNumber; return this; }
        public Builder issuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; return this; }
        public Builder activatedAt(LocalDateTime activatedAt) { this.activatedAt = activatedAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public CardDTO build() {
            CardDTO dto = new CardDTO();
            dto.setCardId(cardId);
            dto.setCardNumber(cardNumber);
            dto.setFullCardNumber(fullCardNumber);
            dto.setCardHolderName(cardHolderName);
            dto.setCardType(cardType);
            dto.setCardStatus(cardStatus);
            dto.setExpiryDate(expiryDate);
            dto.setDailyLimit(dailyLimit);
            dto.setAvailableBalance(availableBalance);
            dto.setCreditLimit(creditLimit);
            dto.setCurrentBalance(currentBalance);
            dto.setUserId(userId);
            dto.setUserName(userName);
            dto.setUserEmail(userEmail);
            dto.setAccountId(accountId);
            dto.setAccountNumber(accountNumber);
            dto.setFullAccountNumber(fullAccountNumber);
            dto.setIssuedDate(issuedDate);
            dto.setActivatedAt(activatedAt);
            dto.setCreatedAt(createdAt);
            dto.setUpdatedAt(updatedAt);
            return dto;
        }
    }

    // toString method
    @Override
    public String toString() {
        return "CardDTO{" +
                "cardId=" + cardId +
                ", cardNumber='" + maskCardNumber() + '\'' +
                ", cardHolderName='" + cardHolderName + '\'' +
                ", cardType=" + cardType +
                ", cardStatus=" + cardStatus +
                ", expiryDate=" + expiryDate +
                ", userId=" + userId +
                ", accountId=" + accountId +
                '}';
    }

    private String maskCardNumber() {
        if (cardNumber == null || cardNumber.length() < 12) {
            return "****";
        }
        return cardNumber.substring(0, 4) + "********" + cardNumber.substring(cardNumber.length() - 4);
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardDTO cardDTO = (CardDTO) o;
        return cardId != null && cardId.equals(cardDTO.cardId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}