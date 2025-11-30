// src/main/java/com/cognitive/banking/repository/LoanPaymentRepository.java
package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.LoanPayment;
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
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, UUID> {

    List<LoanPayment> findByLoanLoanId(UUID loanId);

    List<LoanPayment> findByPaymentStatus(String paymentStatus);

    @Query("SELECT lp FROM LoanPayment lp WHERE lp.loan.loanId = :loanId ORDER BY lp.paymentNumber")
    List<LoanPayment> findPaymentsByLoanIdOrderByPaymentNumber(@Param("loanId") UUID loanId);

    @Query("SELECT lp FROM LoanPayment lp WHERE lp.dueDate <= :date AND lp.paymentStatus = 'PENDING'")
    List<LoanPayment> findOverduePayments(@Param("date") LocalDate date);

    @Query("SELECT SUM(lp.totalPayment) FROM LoanPayment lp WHERE lp.loan.loanId = :loanId AND lp.paymentStatus = 'PAID'")
    BigDecimal getTotalPaidAmountByLoanId(@Param("loanId") UUID loanId);

    @Query("SELECT MAX(lp.paymentNumber) FROM LoanPayment lp WHERE lp.loan.loanId = :loanId")
    Integer findLastPaymentNumberByLoanId(@Param("loanId") UUID loanId);

    @Query("SELECT lp FROM LoanPayment lp WHERE lp.loan.loanId = :loanId AND lp.paymentStatus = 'PENDING' ORDER BY lp.dueDate ASC")
    List<LoanPayment> findPendingPaymentsByLoanId(@Param("loanId") UUID loanId);

    @Query("SELECT lp FROM LoanPayment lp WHERE lp.loan.user.userId = :userId")
    List<LoanPayment> findPaymentsByUserId(@Param("userId") UUID userId);

    Optional<LoanPayment> findByLoanLoanIdAndPaymentNumber(UUID loanId, Integer paymentNumber);
}