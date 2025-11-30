// src/main/java/com/cognitive/banking/repository/LoanRepository.java
package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.Loan;
import com.cognitive.banking.domain.enums.LoanStatus;
import com.cognitive.banking.domain.enums.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {

    Optional<Loan> findByLoanNumber(String loanNumber);

    List<Loan> findByUserUserId(UUID userId);

    List<Loan> findByLoanType(LoanType loanType);

    List<Loan> findByLoanStatus(LoanStatus loanStatus);

    List<Loan> findByUserUserIdAndLoanStatus(UUID userId, LoanStatus loanStatus);

    boolean existsByLoanNumber(String loanNumber);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user.userId = :userId AND l.loanStatus = 'ACTIVE'")
    long countActiveLoansByUserId(@Param("userId") UUID userId);

    @Query("SELECT SUM(l.remainingBalance) FROM Loan l WHERE l.user.userId = :userId AND l.loanStatus IN ('ACTIVE', 'DELINQUENT')")
    BigDecimal getTotalOutstandingBalanceByUserId(@Param("userId") UUID userId);

    @Query("SELECT l FROM Loan l WHERE l.nextPaymentDate <= :date AND l.loanStatus = 'ACTIVE'")
    List<Loan> findLoansWithDuePayments(@Param("date") LocalDate date);

    @Query("SELECT l FROM Loan l WHERE l.maturityDate <= :date AND l.loanStatus = 'ACTIVE'")
    List<Loan> findMaturedLoans(@Param("date") LocalDate date);
}