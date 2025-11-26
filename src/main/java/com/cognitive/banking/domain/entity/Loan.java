// src/main/java/com/cognitive/banking/domain/entity/Loan.java
package com.cognitive.banking.domain.entity;

import com.cognitive.banking.domain.enums.LoanType;
import com.cognitive.banking.domain.enums.LoanStatus;
import com.cognitive.banking.domain.enums.Currency;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Data
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    // Loan Details
    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.ZAR;

    // Financials
    @Column(precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal outstandingBalance;

    @Column(precision = 5, scale = 4)
    private BigDecimal interestRate;

    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyRepayment;

    // Terms
    private Integer termMonths;
    private LocalDate disbursementDate;
    private LocalDate maturityDate;
    private LocalDate nextPaymentDate;

    // Collateral
    private String collateralDescription;
    private BigDecimal collateralValue;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (outstandingBalance == null) {
            outstandingBalance = principalAmount;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}