package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.LoanType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class CreateLoanRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Account ID is required")
    private UUID accountId;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Principal amount is required")
    private BigDecimal principalAmount;

    @NotNull(message = "Interest rate is required")
    private BigDecimal interestRate;

    @NotNull(message = "Term months is required")
    private Integer termMonths;

    private String purpose;
    private String collateralDescription;

    // Constructors
    public CreateLoanRequest() {}

    public CreateLoanRequest(UUID userId, UUID accountId, LoanType loanType,
                             BigDecimal principalAmount, BigDecimal interestRate,
                             Integer termMonths, String purpose, String collateralDescription) {
        this.userId = userId;
        this.accountId = accountId;
        this.loanType = loanType;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.purpose = purpose;
        this.collateralDescription = collateralDescription;
    }

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanType loanType) { this.loanType = loanType; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getCollateralDescription() { return collateralDescription; }
    public void setCollateralDescription(String collateralDescription) { this.collateralDescription = collateralDescription; }
}