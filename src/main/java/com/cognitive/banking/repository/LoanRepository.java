// src/main/java/com/cognitive/banking/repository/LoanRepository.java
package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.Loan;
import com.cognitive.banking.domain.enums.LoanStatus;
import com.cognitive.banking.domain.enums.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByLoanType(LoanType loanType);
    List<Loan> findByUserUsername(String username);
}