// src/main/java/com/cognitive/banking/repository/TransactionRepository.java
package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.Transaction;
import com.cognitive.banking.domain.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountAccountNumberOrderByCreatedAtDesc(String accountNumber);
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);
    List<Transaction> findByType(TransactionType type);
    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.account.user.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.id = :accountId AND t.type = 'DEPOSIT' AND t.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalDepositsForPeriod(@Param("accountId") Long accountId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);
}