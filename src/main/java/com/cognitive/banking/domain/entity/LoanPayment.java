package com.cognitive.banking.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loan_payments")
public class LoanPayment {

    public enum PaymentStatus {
        PENDING, PAID, LATE, PARTIAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "payment_number", nullable = false)
    private Integer paymentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "principal_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal interestAmount;

    @Column(name = "total_payment", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalPayment;

    @Column(name = "remaining_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal remainingBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "late_fee", precision = 15, scale = 2)
    private BigDecimal lateFee;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public LoanPayment() {
        this.createdAt = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.PENDING;
        this.lateFee = BigDecimal.ZERO;
    }

    // Getters and Setters
    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public Loan getLoan() { return loan; }
    public void setLoan(Loan loan) { this.loan = loan; }

    public Integer getPaymentNumber() { return paymentNumber; }
    public void setPaymentNumber(Integer paymentNumber) { this.paymentNumber = paymentNumber; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public BigDecimal getInterestAmount() { return interestAmount; }
    public void setInterestAmount(BigDecimal interestAmount) { this.interestAmount = interestAmount; }

    public BigDecimal getTotalPayment() { return totalPayment; }
    public void setTotalPayment(BigDecimal totalPayment) { this.totalPayment = totalPayment; }

    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }

    public BigDecimal getLateFee() { return lateFee; }
    public void setLateFee(BigDecimal lateFee) { this.lateFee = lateFee; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void markAsPaid() {
        this.paymentStatus = PaymentStatus.PAID;
        this.paidDate = LocalDate.now();
    }
}