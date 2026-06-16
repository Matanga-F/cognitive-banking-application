// src/main/java/com/cognitive/banking/repository/AccountRepository.java
package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.Account;
import com.cognitive.banking.domain.enums.AccountStatus;
import com.cognitive.banking.domain.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    // ==================== BASIC QUERIES ====================

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserUserId(UUID userId);

    List<Account> findByAccountType(AccountType accountType);

    List<Account> findByAccountStatus(AccountStatus accountStatus);

    List<Account> findByUserUserIdAndAccountStatus(UUID userId, AccountStatus accountStatus);
    boolean existsByAccountIdAndUserUserId(UUID accountId, UUID userId);
    boolean existsByAccountNumber(String accountNumber);

    // ==================== BALANCE QUERIES ====================

    @Query("SELECT COUNT(a) FROM Account a WHERE a.user.userId = :userId AND a.accountStatus = 'ACTIVE'")
    long countActiveAccountsByUserId(@Param("userId") UUID userId);

    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user.userId = :userId AND a.accountStatus = 'ACTIVE'")
    BigDecimal getTotalBalanceByUserId(@Param("userId") UUID userId);

    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber AND a.routingNumber = :routingNumber")
    Optional<Account> findByAccountAndRoutingNumber(@Param("accountNumber") String accountNumber,
                                                    @Param("routingNumber") String routingNumber);

    long countByAccountStatus(AccountStatus accountStatus);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.accountStatus = 'ACTIVE'")
    BigDecimal getTotalBalanceAcrossAllAccounts();

    // ==================== METRICS & MONITORING QUERIES ====================

    /**
     * Count accounts by type
     */
    @Query("SELECT a.accountType, COUNT(a) FROM Account a GROUP BY a.accountType")
    List<Object[]> countAccountsByType();

    /**
     * Count accounts by status
     */
    @Query("SELECT a.accountStatus, COUNT(a) FROM Account a GROUP BY a.accountStatus")
    List<Object[]> countAccountsByStatus();

    /**
     * Get total balance by account type
     */
    @Query("SELECT a.accountType, COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.accountStatus = 'ACTIVE' GROUP BY a.accountType")
    List<Object[]> getTotalBalanceByAccountType();

    /**
     * Get average balance across all accounts
     */
    @Query("SELECT COALESCE(AVG(a.balance), 0) FROM Account a WHERE a.accountStatus = 'ACTIVE'")
    BigDecimal getAverageBalance();

    /**
     * Get accounts created in last N days
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.createdAt >= :sinceDate")
    long countAccountsCreatedSince(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Get accounts created today
     */
    default long countAccountsCreatedToday() {
        return countAccountsCreatedSince(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
    }

    /**
     * Get accounts created in last hour
     */
    default long countAccountsCreatedLastHour() {
        return countAccountsCreatedSince(LocalDateTime.now().minusHours(1));
    }

    /**
     * Get accounts by user with balance range
     */
    @Query("SELECT a FROM Account a WHERE a.user.userId = :userId AND a.balance BETWEEN :minBalance AND :maxBalance")
    List<Account> findAccountsByBalanceRange(@Param("userId") UUID userId,
                                             @Param("minBalance") BigDecimal minBalance,
                                             @Param("maxBalance") BigDecimal maxBalance);

    /**
     * Get top N accounts by balance
     */
    @Query("SELECT a FROM Account a WHERE a.accountStatus = 'ACTIVE' ORDER BY a.balance DESC")
    List<Account> findTopAccountsByBalance(org.springframework.data.domain.Pageable pageable);

    /**
     * Get account distribution by currency
     */
    @Query("SELECT a.currency, COUNT(a) FROM Account a GROUP BY a.currency")
    List<Object[]> countAccountsByCurrency();

    /**
     * Get total overdraft used across all accounts
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN a.balance < 0 THEN ABS(a.balance) ELSE 0 END), 0) FROM Account a")
    BigDecimal getTotalOverdraftUsed();

    /**
     * Count accounts with negative balance (overdrawn)
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.balance < 0 AND a.accountStatus = 'ACTIVE'")
    long countOverdrawnAccounts();

    /**
     * Count accounts with high balance (over threshold)
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.balance > :threshold AND a.accountStatus = 'ACTIVE'")
    long countHighBalanceAccounts(@Param("threshold") BigDecimal threshold);

    /**
     * Get accounts that haven't had transactions in N days (dormant)
     * Note: Requires a lastTransactionDate field on Account entity
     */
    @Query("SELECT a FROM Account a WHERE a.accountStatus = 'ACTIVE' AND a.updatedAt < :sinceDate")
    List<Account> findDormantAccounts(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Get daily account creation trend
     */
    @Query("SELECT DATE(a.createdAt), COUNT(a) FROM Account a WHERE a.createdAt >= :startDate GROUP BY DATE(a.createdAt) ORDER BY DATE(a.createdAt)")
    List<Object[]> getAccountCreationTrend(@Param("startDate") LocalDateTime startDate);

    /**
     * Get comprehensive account statistics for dashboard
     */
    @Query("SELECT " +
            "COUNT(a) as totalAccounts," +
            "SUM(CASE WHEN a.accountStatus = 'ACTIVE' THEN 1 ELSE 0 END) as activeAccounts," +
            "SUM(CASE WHEN a.accountStatus = 'INACTIVE' THEN 1 ELSE 0 END) as inactiveAccounts," +
            "SUM(CASE WHEN a.accountStatus = 'CLOSED' THEN 1 ELSE 0 END) as closedAccounts," +
            "SUM(CASE WHEN a.accountStatus = 'FROZEN' THEN 1 ELSE 0 END) as frozenAccounts," +
            "COALESCE(SUM(a.balance), 0) as totalBalance," +
            "COALESCE(AVG(a.balance), 0) as averageBalance," +
            "COALESCE(MAX(a.balance), 0) as maxBalance," +
            "COALESCE(MIN(a.balance), 0) as minBalance " +
            "FROM Account a")
    Object[] getAccountStatistics();

    /**
     * Get account statistics by user (for user dashboard)
     */
    @Query("SELECT " +
            "COUNT(a) as totalAccounts," +
            "SUM(CASE WHEN a.accountStatus = 'ACTIVE' THEN 1 ELSE 0 END) as activeAccounts," +
            "COALESCE(SUM(a.balance), 0) as totalBalance " +
            "FROM Account a WHERE a.user.userId = :userId")
    Object[] getAccountStatisticsByUser(@Param("userId") UUID userId);

    /**
     * Find accounts with balance below minimum required (for monitoring)
     */
    @Query("SELECT a FROM Account a WHERE a.balance < :minimumBalance AND a.accountStatus = 'ACTIVE'")
    List<Account> findAccountsBelowMinimumBalance(@Param("minimumBalance") BigDecimal minimumBalance);

    /**
     * Get account type distribution with balances
     */
    @Query("SELECT a.accountType, COUNT(a), COALESCE(SUM(a.balance), 0), COALESCE(AVG(a.balance), 0) " +
            "FROM Account a WHERE a.accountStatus = 'ACTIVE' GROUP BY a.accountType")
    List<Object[]> getAccountTypeDistribution();
}