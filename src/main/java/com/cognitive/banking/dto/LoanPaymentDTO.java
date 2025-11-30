// src/main/java/com/cognitive/banking/dto/LoanPaymentDTO.java
package com.cognitive.banking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class LoanPaymentDTO {
    private UUID paymentId;
    private UUID loanId;
    private String loanNumber;
    private Integer paymentNumber;
    private LocalDate paymentDate;
    private LocalDate dueDate;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal totalPayment;
    private BigDecimal remainingBalance;
    private String paymentStatus;
    private LocalDate paidDate;
    private BigDecimal lateFee;
    private LocalDateTime createdAt;

    // Constructors
    public LoanPaymentDTO() {}

    public LoanPaymentDTO(UUID paymentId, UUID loanId, String loanNumber, Integer paymentNumber,
                          LocalDate paymentDate, LocalDate dueDate, BigDecimal principalAmount,
                          BigDecimal interestAmount, BigDecimal totalPayment, BigDecimal remainingBalance,
                          String paymentStatus, LocalDate paidDate, BigDecimal lateFee, LocalDateTime createdAt) {
        this.paymentId = paymentId;
        this.loanId = loanId;
        this.loanNumber = loanNumber;
        this.paymentNumber = paymentNumber;
        this.paymentDate = paymentDate;
        this.dueDate = dueDate;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.totalPayment = totalPayment;
        this.remainingBalance = remainingBalance;
        this.paymentStatus = paymentStatus;
        this.paidDate = paidDate;
        this.lateFee = lateFee;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public UUID getLoanId() { return loanId; }
    public void setLoanId(UUID loanId) { this.loanId = loanId; }

    public String getLoanNumber() { return loanNumber; }
    public void setLoanNumber(String loanNumber) { this.loanNumber = loanNumber; }

    public Integer getPaymentNumber() { return paymentNumber; }
    public void setPaymentNumber(Integer paymentNumber) { this.paymentNumber = paymentNumber; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public BigDecimal getInterestAmount() { return interestAmount; }
    public void setInterestAmount(BigDecimal interestAmount) { this.interestAmount = interestAmount; }

    public BigDecimal getTotalPayment() { return totalPayment; }
    public void setTotalPayment(BigDecimal totalPayment) { this.totalPayment = totalPayment; }

    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }

    public BigDecimal getLateFee() { return lateFee; }
    public void setLateFee(BigDecimal lateFee) { this.lateFee = lateFee; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}