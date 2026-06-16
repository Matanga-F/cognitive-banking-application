package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.Account;
import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.AccountStatus;
import com.cognitive.banking.domain.enums.AccountType;
import com.cognitive.banking.dto.AccountDTO;
import com.cognitive.banking.dto.CreateAccountRequest;
import com.cognitive.banking.dto.UpdateAccountRequest;
import com.cognitive.banking.monitoring.metrics.BankingMetrics;
import com.cognitive.banking.monitoring.metrics.MetricNames;
import com.cognitive.banking.monitoring.metrics.MetricTags;
import com.cognitive.banking.repository.AccountRepository;
import com.cognitive.banking.repository.UserRepository;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BankingMetrics bankingMetrics;

    private static final String ROUTING_NUMBER = "021000021";
    private static final String ACCOUNT_CACHE = "accounts";

    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository,
                          BankingMetrics bankingMetrics) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.bankingMetrics = bankingMetrics;
    }

    // ============================================
    // CREATE ACCOUNT - With Metrics
    // ============================================
    public AccountDTO createAccount(CreateAccountRequest request) {
        logger.info("Creating new account for user ID: {}", request.getUserId());
        Timer.Sample sample = bankingMetrics.startAccountCreationTimer();

        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

            String accountNumber = generateAccountNumber();

            Account account = new Account();
            account.setAccountNumber(accountNumber);
            account.setRoutingNumber(ROUTING_NUMBER);
            account.setAccountType(request.getAccountType());
            account.setCurrency(request.getCurrency());
            account.setUser(user);
            account.setBalance(request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO);
            account.setOverdraftLimit(request.getOverdraftLimit() != null ? request.getOverdraftLimit() : BigDecimal.ZERO);
            account.setInterestRate(request.getInterestRate() != null ? request.getInterestRate() : BigDecimal.ZERO);
            account.setAccountStatus(AccountStatus.ACTIVE);
            account.setCreatedAt(LocalDateTime.now());

            Account savedAccount = accountRepository.save(account);

            // Record metrics
            bankingMetrics.recordAccountCreated(
                    savedAccount.getAccountType().name(),
                    savedAccount.getCurrency(),
                    savedAccount.getBalance()
            );
            bankingMetrics.updateAccountMetrics(
                    accountRepository.count(),
                    accountRepository.countByAccountStatus(AccountStatus.ACTIVE),
                    accountRepository.getTotalBalanceAcrossAllAccounts()
            );

            bankingMetrics.stopAccountCreationTimer(sample);

            logger.info("Account created successfully with number: {}", savedAccount.getAccountNumber());

            return convertToDTO(savedAccount);

        } catch (Exception e) {
            bankingMetrics.stopAccountCreationTimer(sample);
            bankingMetrics.recordAccountCreationFailure(request.getAccountType().name());
            logger.error("Account creation failed: {}", e.getMessage());
            throw e;
        }
    }

    // ============================================
    // GET ACCOUNT BY ID - With Metrics
    // ============================================
    @Cacheable(value = ACCOUNT_CACHE, key = "#accountId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<AccountDTO> getAccountById(UUID accountId) {
        logger.debug("Fetching account by ID: {}", accountId);
        bankingMetrics.recordAccountQuery("by_id");

        Timer.Sample sample = bankingMetrics.startAccountQueryTimer();
        try {
            Optional<AccountDTO> result = accountRepository.findById(accountId).map(this::convertToDTO);
            bankingMetrics.stopAccountQueryTimer(sample);
            return result;
        } catch (Exception e) {
            bankingMetrics.stopAccountQueryTimer(sample);
            throw e;
        }
    }

    // ============================================
    // GET ACCOUNT BY NUMBER - With Metrics
    // ============================================
    @Transactional(readOnly = true)
    public Optional<AccountDTO> getAccountByNumber(String accountNumber) {
        logger.debug("Fetching account by number: {}", maskAccountNumber(accountNumber));
        bankingMetrics.recordAccountQuery("by_number");

        Timer.Sample sample = bankingMetrics.startAccountQueryTimer();
        try {
            Optional<AccountDTO> result = accountRepository.findByAccountNumber(accountNumber).map(this::convertToDTO);
            bankingMetrics.stopAccountQueryTimer(sample);
            return result;
        } catch (Exception e) {
            bankingMetrics.stopAccountQueryTimer(sample);
            throw e;
        }
    }

    // ============================================
    // GET ACCOUNTS BY USER ID - With Metrics
    // ============================================
    @Transactional(readOnly = true)
    public List<AccountDTO> getAccountsByUserId(UUID userId) {
        logger.debug("Fetching accounts for user ID: {}", userId);
        bankingMetrics.recordAccountQuery("by_user_id");

        return accountRepository.findByUserUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public String getUserEmailById(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getEmail)
                .orElse(null);
    }

    public UUID getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElse(null);
    }

    // ============================================
    // GET ALL ACCOUNTS - With Metrics
    // ============================================
    @Transactional(readOnly = true)
    public List<AccountDTO> getAllAccounts() {
        logger.info("Fetching all accounts");
        bankingMetrics.recordAccountQuery("all");

        return accountRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // UPDATE ACCOUNT - With Metrics
    // ============================================
    @CacheEvict(value = ACCOUNT_CACHE, key = "#accountId")
    public AccountDTO updateAccount(UUID accountId, UpdateAccountRequest request) {
        logger.info("Updating account with ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

        if (request.getOverdraftLimit() != null) {
            account.setOverdraftLimit(request.getOverdraftLimit());
        }
        if (request.getInterestRate() != null) {
            account.setInterestRate(request.getInterestRate());
        }
        if (request.getAccountStatus() != null) {
            account.setAccountStatus(request.getAccountStatus());
        }
        account.setUpdatedAt(LocalDateTime.now());

        Account updatedAccount = accountRepository.save(account);

        bankingMetrics.recordAccountUpdate(account.getAccountType().name());

        logger.info("Account updated successfully: {}", accountId);

        return convertToDTO(updatedAccount);
    }

    // ============================================
    // UPDATE ACCOUNT STATUS - With Metrics
    // ============================================
    public AccountDTO updateAccountStatus(UUID accountId, AccountStatus status) {
        logger.info("Updating account status to {} for account ID: {}", status, accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

        AccountStatus oldStatus = account.getAccountStatus();
        account.setAccountStatus(status);
        account.setUpdatedAt(LocalDateTime.now());
        Account updatedAccount = accountRepository.save(account);

        bankingMetrics.recordAccountStatusChange(oldStatus.name(), status.name());
        bankingMetrics.updateAccountMetrics(
                accountRepository.count(),
                accountRepository.countByAccountStatus(AccountStatus.ACTIVE),
                accountRepository.getTotalBalanceAcrossAllAccounts()
        );

        logger.info("Account status updated successfully from {} to {}", oldStatus, status);
        return convertToDTO(updatedAccount);
    }

    // ============================================
    // CLOSE ACCOUNT - With Metrics
    // ============================================
    @CacheEvict(value = ACCOUNT_CACHE, key = "#accountId")
    public void closeAccount(UUID accountId) {
        logger.info("Closing account with ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

        account.setAccountStatus(AccountStatus.CLOSED);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        bankingMetrics.recordAccountClosed(account.getAccountType().name());
        bankingMetrics.updateAccountMetrics(
                accountRepository.count(),
                accountRepository.countByAccountStatus(AccountStatus.ACTIVE),
                accountRepository.getTotalBalanceAcrossAllAccounts()
        );

        logger.info("Account closed successfully: {}", accountId);
    }

    // ============================================
    // GET ACCOUNT BALANCE - With Metrics
    // ============================================
    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(UUID accountId) {
        bankingMetrics.recordBalanceCheck();

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

        bankingMetrics.recordBalanceAmount(account.getBalance());
        return account.getBalance();
    }

    // ============================================
    // GET TOTAL BALANCE BY USER ID - With Metrics
    // ============================================
    @Transactional(readOnly = true)
    public BigDecimal getTotalBalanceByUserId(UUID userId) {
        BigDecimal totalBalance = accountRepository.getTotalBalanceByUserId(userId);
        BigDecimal result = totalBalance != null ? totalBalance : BigDecimal.ZERO;

        bankingMetrics.recordTotalBalanceCheck(userId, result);

        return result;
    }

    // ============================================
    // GET ACTIVE ACCOUNTS COUNT - With Metrics
    // ============================================
    @Transactional(readOnly = true)
    public long getActiveAccountsCountByUserId(UUID userId) {
        return accountRepository.countActiveAccountsByUserId(userId);
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private String generateAccountNumber() {
        String accountNumber;
        do {
            long number = (long) (Math.random() * 1_000_000_000_000L);
            accountNumber = String.format("%012d", number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    private AccountDTO convertToDTO(Account account) {
        String userName = account.getUser().getFirstName() + " " + account.getUser().getLastName();

        AccountDTO dto = new AccountDTO();
        dto.setAccountId(account.getAccountId());
        dto.setAccountNumber(maskAccountNumber(account.getAccountNumber()));
        dto.setFullAccountNumber(account.getAccountNumber());
        dto.setRoutingNumber(account.getRoutingNumber());
        dto.setAccountType(account.getAccountType());
        dto.setAccountStatus(account.getAccountStatus());
        dto.setBalance(account.getBalance());
        dto.setCurrency(account.getCurrency());
        dto.setOverdraftLimit(account.getOverdraftLimit());
        dto.setInterestRate(account.getInterestRate());
        dto.setUserId(account.getUser().getUserId());
        dto.setUserName(userName);
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());

        return dto;
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
            return accountNumber;
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}