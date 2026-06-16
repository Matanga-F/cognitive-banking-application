package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.Transaction;
import com.cognitive.banking.domain.enums.TransactionStatus;
import com.cognitive.banking.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    List<Transaction> findByTransactionStatus(TransactionStatus status);

    List<Transaction> findByTransactionType(TransactionType type);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.accountId = :accountId OR t.toAccount.accountId = :accountId ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.accountId = :accountId OR t.toAccount.accountId = :accountId) AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountIdAndDateRange(@Param("accountId") UUID accountId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT * FROM transactions WHERE from_account_id = :accountId OR to_account_id = :accountId ORDER BY transaction_date DESC LIMIT :limit", nativeQuery = true)
    List<Transaction> findTopNByAccountId(@Param("accountId") UUID accountId, @Param("limit") int limit);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.accountId = :accountId AND t.transactionType = :type ORDER BY t.transactionDate DESC")
    List<Transaction> findByFromAccountAccountIdAndTransactionType(@Param("accountId") UUID accountId,
                                                                   @Param("type") TransactionType type);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE (t.fromAccount.accountId = :accountId OR t.toAccount.accountId = :accountId) AND t.transactionDate BETWEEN :startDate AND :endDate AND t.transactionStatus = 'COMPLETED'")
    BigDecimal getTotalTransactionAmountByAccountIdAndDateRange(@Param("accountId") UUID accountId,
                                                                @Param("startDate") LocalDateTime startDate,
                                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.fromAccount.accountId = :accountId AND t.transactionType = :type AND t.transactionDate BETWEEN :startDate AND :endDate AND t.transactionStatus = 'COMPLETED'")
    BigDecimal getTotalAmountByAccountIdAndDateRange(@Param("accountId") UUID accountId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate,
                                                     @Param("type") TransactionType type);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.fromAccount.accountId = :accountId AND t.transactionDate > :since")
    long countTransactionsSince(@Param("accountId") UUID accountId, @Param("since") LocalDateTime since);

    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate")
    Page<Transaction> findByTransactionDateBetween(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate,
                                                   Pageable pageable);

    @Modifying
    @Query("UPDATE Transaction t SET t.transactionStatus = 'FAILED' WHERE t.transactionStatus = 'PENDING' AND t.transactionDate < :timeout")
    int timeoutPendingTransactions(@Param("timeout") LocalDateTime timeout);
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.user.userId = :userId OR t.toAccount.user.userId = :userId")
    List<Transaction> findByUserUserId(@Param("userId") UUID userId);

}