// src/main/java/com/cognitive/banking/service/AccountService.java
package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.Account;
import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.AccountStatus;
import com.cognitive.banking.domain.enums.AccountType;
import com.cognitive.banking.dto.AccountDTO;
import com.cognitive.banking.dto.CreateAccountRequest;
import com.cognitive.banking.dto.UpdateAccountRequest;
import com.cognitive.banking.repository.AccountRepository;
import com.cognitive.banking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String ROUTING_NUMBER = "021000021"; // Example routing number

    public AccountDTO createAccount(CreateAccountRequest request) {
        System.out.println("Creating new account for user ID: " + request.getUserId());

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        // Generate unique account number
        String accountNumber = generateAccountNumber();

        // Create account entity
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setRoutingNumber(ROUTING_NUMBER);
        account.setAccountType(request.getAccountType());
        account.setCurrency(request.getCurrency());
        account.setUser(user);

        // Set initial balance
        BigDecimal initialDeposit = request.getInitialDeposit() != null ?
                request.getInitialDeposit() : BigDecimal.ZERO;
        account.setBalance(initialDeposit);

        // Set optional fields
        if (request.getOverdraftLimit() != null) {
            account.setOverdraftLimit(request.getOverdraftLimit());
        }
        if (request.getInterestRate() != null) {
            account.setInterestRate(request.getInterestRate());
        }

        Account savedAccount = accountRepository.save(account);
        System.out.println("Account created successfully with number: " + savedAccount.getAccountNumber());

        return convertToDTO(savedAccount);
    }

    @Transactional(readOnly = true)
    public Optional<AccountDTO> getAccountById(UUID accountId) {
        System.out.println("Fetching account by ID: " + accountId);
        return accountRepository.findById(accountId)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AccountDTO> getAccountByNumber(String accountNumber) {
        System.out.println("Fetching account by number: " + accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getAccountsByUserId(UUID userId) {
        System.out.println("Fetching accounts for user ID: " + userId);
        return accountRepository.findByUserUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getAllAccounts() {
        System.out.println("Fetching all accounts");
        return accountRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AccountDTO updateAccount(UUID accountId, UpdateAccountRequest request) {
        System.out.println("Updating account with ID: " + accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

        // Update fields if provided
        if (request.getOverdraftLimit() != null) {
            account.setOverdraftLimit(request.getOverdraftLimit());
        }
        if (request.getInterestRate() != null) {
            account.setInterestRate(request.getInterestRate());
        }
        if (request.getAccountStatus() != null) {
            account.setAccountStatus(request.getAccountStatus());
        }

        Account updatedAccount = accountRepository.save(account);
        System.out.println("Account updated successfully");

        return convertToDTO(updatedAccount);
    }

    public AccountDTO updateAccountStatus(UUID accountId, AccountStatus status) {
        System.out.println("Updating account status to " + status + " for account ID: " + accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

        account.setAccountStatus(status);
        Account updatedAccount = accountRepository.save(account);

        System.out.println("Account status updated successfully");
        return convertToDTO(updatedAccount);
    }

    public void deleteAccount(UUID accountId) {
        System.out.println("Deleting account with ID: " + accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

        // Instead of hard delete, set status to CLOSED
        account.setAccountStatus(AccountStatus.CLOSED);
        accountRepository.save(account);

        System.out.println("Account closed successfully");
    }

    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        return account.getBalance();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalBalanceByUserId(UUID userId) {
        BigDecimal totalBalance = accountRepository.getTotalBalanceByUserId(userId);
        return totalBalance != null ? totalBalance : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long getActiveAccountsCountByUserId(UUID userId) {
        return accountRepository.countActiveAccountsByUserId(userId);
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            // Generate 12-digit account number
            long number = (long) (Math.random() * 1_000_000_000_000L);
            accountNumber = String.format("%012d", number);
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private AccountDTO convertToDTO(Account account) {
        String userName = account.getUser().getFirstName() + " " + account.getUser().getLastName();

        return new AccountDTO(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getRoutingNumber(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getBalance(),
                account.getCurrency(),
                account.getOverdraftLimit(),
                account.getInterestRate(),
                account.getUser().getUserId(),
                userName,
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}