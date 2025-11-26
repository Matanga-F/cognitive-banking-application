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
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByUserId(Long userId);
    List<Account> findByUserUsername(String username);
    List<Account> findByAccountType(AccountType accountType);
    List<Account> findByStatus(AccountStatus status);
    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a WHERE a.balance < :amount")
    List<Account> findAccountsWithBalanceLessThan(@Param("amount") BigDecimal amount);

    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user.id = :userId")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);
}