// src/main/java/com/cognitive/banking/domain/entity/Loan.java
package com.cognitive.banking.domain.entity;

import com.cognitive.banking.domain.enums.LoanStatus;
import com.cognitive.banking.domain.enums.LoanType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loans")
public class Loan {
    @Id
    @GeneratedValue
    @Column(name = "loan_id")
    private UUID loanId;

    @Column(name = "loan_number", unique = true, nullable = false)
    private String loanNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false)
    private LoanType loanType;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_status", nullable = false)
    private LoanStatus loanStatus;

    @Column(name = "principal_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "remaining_term_months", nullable = false)
    private Integer remainingTermMonths;

    @Column(name = "monthly_payment", precision = 15, scale = 2, nullable = false)
    private BigDecimal monthlyPayment;

    @Column(name = "remaining_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal remainingBalance;

    @Column(name = "total_interest_paid", precision = 15, scale = 2)
    private BigDecimal totalInterestPaid;

    @Column(name = "total_amount_paid", precision = 15, scale = 2)
    private BigDecimal totalAmountPaid;

    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "collateral_description")
    private String collateralDescription;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Loan() {
        this.createdAt = LocalDateTime.now();
        this.loanStatus = LoanStatus.PENDING;
        this.totalInterestPaid = BigDecimal.ZERO;
        this.totalAmountPaid = BigDecimal.ZERO;
    }

    public Loan(String loanNumber, LoanType loanType, BigDecimal principalAmount,
                BigDecimal interestRate, Integer termMonths, User user) {
        this();
        this.loanNumber = loanNumber;
        this.loanType = loanType;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.remainingTermMonths = termMonths;
        this.remainingBalance = principalAmount;
        this.user = user;
        this.maturityDate = LocalDate.now().plusMonths(termMonths);
    }

    // Getters and Setters
    public UUID getLoanId() { return loanId; }
    public void setLoanId(UUID loanId) { this.loanId = loanId; }

    public String getLoanNumber() { return loanNumber; }
    public void setLoanNumber(String loanNumber) { this.loanNumber = loanNumber; }

    public LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanType loanType) { this.loanType = loanType; }

    public LoanStatus getLoanStatus() { return loanStatus; }
    public void setLoanStatus(LoanStatus loanStatus) { this.loanStatus = loanStatus; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }

    public Integer getRemainingTermMonths() { return remainingTermMonths; }
    public void setRemainingTermMonths(Integer remainingTermMonths) { this.remainingTermMonths = remainingTermMonths; }

    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public void setMonthlyPayment(BigDecimal monthlyPayment) { this.monthlyPayment = monthlyPayment; }

    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }

    public BigDecimal getTotalInterestPaid() { return totalInterestPaid; }
    public void setTotalInterestPaid(BigDecimal totalInterestPaid) { this.totalInterestPaid = totalInterestPaid; }

    public BigDecimal getTotalAmountPaid() { return totalAmountPaid; }
    public void setTotalAmountPaid(BigDecimal totalAmountPaid) { this.totalAmountPaid = totalAmountPaid; }

    public LocalDate getNextPaymentDate() { return nextPaymentDate; }
    public void setNextPaymentDate(LocalDate nextPaymentDate) { this.nextPaymentDate = nextPaymentDate; }

    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }

    public LocalDate getDisbursementDate() { return disbursementDate; }
    public void setDisbursementDate(LocalDate disbursementDate) { this.disbursementDate = disbursementDate; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getCollateralDescription() { return collateralDescription; }
    public void setCollateralDescription(String collateralDescription) { this.collateralDescription = collateralDescription; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}