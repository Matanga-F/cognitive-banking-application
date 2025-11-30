// src/main/java/com/cognitive/banking/dto/LoanDTO.java
package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.LoanStatus;
import com.cognitive.banking.domain.enums.LoanType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class LoanDTO {
    private UUID loanId;
    private String loanNumber;
    private LoanType loanType;
    private LoanStatus loanStatus;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private Integer remainingTermMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal remainingBalance;
    private BigDecimal totalInterestPaid;
    private BigDecimal totalAmountPaid;
    private LocalDate nextPaymentDate;
    private LocalDate maturityDate;
    private LocalDate disbursementDate;
    private UUID userId;
    private String userName;
    private UUID accountId;
    private String accountNumber;
    private String purpose;
    private String collateralDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public LoanDTO() {}

    public LoanDTO(UUID loanId, String loanNumber, LoanType loanType, LoanStatus loanStatus,
                   BigDecimal principalAmount, BigDecimal interestRate, Integer termMonths,
                   Integer remainingTermMonths, BigDecimal monthlyPayment, BigDecimal remainingBalance,
                   BigDecimal totalInterestPaid, BigDecimal totalAmountPaid, LocalDate nextPaymentDate,
                   LocalDate maturityDate, LocalDate disbursementDate, UUID userId, String userName,
                   UUID accountId, String accountNumber, String purpose, String collateralDescription,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.loanId = loanId;
        this.loanNumber = loanNumber;
        this.loanType = loanType;
        this.loanStatus = loanStatus;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.remainingTermMonths = remainingTermMonths;
        this.monthlyPayment = monthlyPayment;
        this.remainingBalance = remainingBalance;
        this.totalInterestPaid = totalInterestPaid;
        this.totalAmountPaid = totalAmountPaid;
        this.nextPaymentDate = nextPaymentDate;
        this.maturityDate = maturityDate;
        this.disbursementDate = disbursementDate;
        this.userId = userId;
        this.userName = userName;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.purpose = purpose;
        this.collateralDescription = collateralDescription;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getCollateralDescription() { return collateralDescription; }
    public void setCollateralDescription(String collateralDescription) { this.collateralDescription = collateralDescription; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}