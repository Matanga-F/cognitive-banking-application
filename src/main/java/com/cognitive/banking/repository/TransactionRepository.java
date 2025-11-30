// src/main/java/com/cognitive/banking/repository/TransactionRepository.java
package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.Transaction;
import com.cognitive.banking.domain.enums.TransactionStatus;
import com.cognitive.banking.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByTransactionReference(String transactionReference);

    List<Transaction> findByFromAccountAccountId(UUID accountId);

    List<Transaction> findByToAccountAccountId(UUID accountId);

    List<Transaction> findByCardCardId(UUID cardId);

    List<Transaction> findByTransactionType(TransactionType transactionType);

    List<Transaction> findByTransactionStatus(TransactionStatus transactionStatus);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.accountId = :accountId OR t.toAccount.accountId = :accountId ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.accountId = :accountId OR t.toAccount.accountId = :accountId) AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountIdAndDateRange(@Param("accountId") UUID accountId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.accountId = :accountId AND t.transactionType IN ('PURCHASE', 'ATM_WITHDRAWAL', 'TRANSFER_OUT', 'FEE') AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findDebitTransactionsByAccountIdAndDateRange(@Param("accountId") UUID accountId,
                                                                   @Param("startDate") LocalDateTime startDate,
                                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE t.toAccount.accountId = :accountId AND t.transactionType IN ('DEPOSIT', 'TRANSFER_IN', 'REFUND', 'INTEREST') AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findCreditTransactionsByAccountIdAndDateRange(@Param("accountId") UUID accountId,
                                                                    @Param("startDate") LocalDateTime startDate,
                                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.fromAccount.accountId = :accountId AND t.transactionStatus = 'COMPLETED' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalDebitsByAccountIdAndDateRange(@Param("accountId") UUID accountId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.toAccount.accountId = :accountId AND t.transactionStatus = 'COMPLETED' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalCreditsByAccountIdAndDateRange(@Param("accountId") UUID accountId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    Page<Transaction> findByFromAccountAccountId(UUID accountId, Pageable pageable);

    Page<Transaction> findByToAccountAccountId(UUID accountId, Pageable pageable);
}