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
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserUserId(UUID userId);

    List<Account> findByAccountType(AccountType accountType);

    List<Account> findByAccountStatus(AccountStatus accountStatus);

    List<Account> findByUserUserIdAndAccountStatus(UUID userId, AccountStatus accountStatus);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.user.userId = :userId AND a.accountStatus = 'ACTIVE'")
    long countActiveAccountsByUserId(@Param("userId") UUID userId);

    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user.userId = :userId AND a.accountStatus = 'ACTIVE'")
    BigDecimal getTotalBalanceByUserId(@Param("userId") UUID userId);

    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber AND a.routingNumber = :routingNumber")
    Optional<Account> findByAccountAndRoutingNumber(@Param("accountNumber") String accountNumber,
                                                    @Param("routingNumber") String routingNumber);
}