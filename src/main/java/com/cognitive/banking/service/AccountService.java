// src/main/java/com/cognitive/banking/service/AccountService.java
package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.Account;
import com.cognitive.banking.domain.entity.Transaction;
import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.TransactionStatus;
import com.cognitive.banking.domain.enums.TransactionType;
import com.cognitive.banking.dto.AccountDTO;
import com.cognitive.banking.dto.CreateAccountRequest;
import com.cognitive.banking.repository.AccountRepository;
import com.cognitive.banking.repository.TransactionRepository;
import com.cognitive.banking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public AccountDTO createAccount(CreateAccountRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Account account = new Account();
        account.setUser(user);
        account.setAccountType(request.getAccountType());
        account.setCurrency(request.getCurrency());

        BigDecimal initialDeposit = request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO;
        account.setBalance(initialDeposit);
        account.setAvailableBalance(initialDeposit);

        Account savedAccount = accountRepository.save(account);

        // Record initial deposit transaction
        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            Transaction transaction = new Transaction();
            transaction.setAccount(savedAccount);
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setAmount(initialDeposit);
            transaction.setBalanceAfter(savedAccount.getBalance());
            transaction.setDescription("Initial account deposit");
            transactionRepository.save(transaction);
        }

        return convertToDTO(savedAccount);
    }

    public AccountDTO getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        return convertToDTO(account);
    }

    public List<AccountDTO> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AccountDTO deposit(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive");
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        account.setAvailableBalance(newBalance);
        account.setLastActivityAt(java.time.LocalDateTime.now());

        Account updatedAccount = accountRepository.save(account);

        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setAccount(updatedAccount);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription("Cash deposit");
        transactionRepository.save(transaction);

        return convertToDTO(updatedAccount);
    }

    public AccountDTO withdraw(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be positive");
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds. Available: " + account.getAvailableBalance());
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        account.setAvailableBalance(newBalance);
        account.setLastActivityAt(java.time.LocalDateTime.now());

        Account updatedAccount = accountRepository.save(account);

        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setAccount(updatedAccount);
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription("Cash withdrawal");
        transactionRepository.save(transaction);

        return convertToDTO(updatedAccount);
    }
    // Add this method to src/main/java/com/cognitive/banking/service/AccountService.java
    public AccountDTO transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be positive");
        }

        // Get both accounts
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("From account not found: " + fromAccountNumber));

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new RuntimeException("To account not found: " + toAccountNumber));

        // Check if same account
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        // Check sufficient funds
        if (fromAccount.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds for transfer. Available: " + fromAccount.getAvailableBalance());
        }

        // Perform transfer
        BigDecimal fromNewBalance = fromAccount.getBalance().subtract(amount);
        BigDecimal toNewBalance = toAccount.getBalance().add(amount);

        fromAccount.setBalance(fromNewBalance);
        fromAccount.setAvailableBalance(fromNewBalance);
        fromAccount.setLastActivityAt(java.time.LocalDateTime.now());

        toAccount.setBalance(toNewBalance);
        toAccount.setAvailableBalance(toNewBalance);
        toAccount.setLastActivityAt(java.time.LocalDateTime.now());

        // Save both accounts
        Account updatedFromAccount = accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Record transactions for both accounts
        Transaction fromTransaction = new Transaction();
        fromTransaction.setAccount(updatedFromAccount);
        fromTransaction.setType(TransactionType.TRANSFER);
        fromTransaction.setStatus(TransactionStatus.COMPLETED);
        fromTransaction.setAmount(amount.negate()); // Negative for outgoing
        fromTransaction.setBalanceAfter(fromNewBalance);
        fromTransaction.setRecipientAccountNumber(toAccountNumber);
        fromTransaction.setRecipientName(toAccount.getUser().getFirstName() + " " + toAccount.getUser().getLastName());
        fromTransaction.setDescription("Transfer to " + toAccountNumber);
        transactionRepository.save(fromTransaction);

        Transaction toTransaction = new Transaction();
        toTransaction.setAccount(toAccount);
        toTransaction.setType(TransactionType.TRANSFER);
        toTransaction.setStatus(TransactionStatus.COMPLETED);
        toTransaction.setAmount(amount); // Positive for incoming
        toTransaction.setBalanceAfter(toNewBalance);
        toTransaction.setRecipientAccountNumber(fromAccountNumber);
        toTransaction.setRecipientName(fromAccount.getUser().getFirstName() + " " + fromAccount.getUser().getLastName());
        toTransaction.setDescription("Transfer from " + fromAccountNumber);
        transactionRepository.save(toTransaction);

        return convertToDTO(updatedFromAccount);
    }

    public BigDecimal getAccountBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        return account.getBalance();
    }

    private AccountDTO convertToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setUserId(account.getUser().getId());
        dto.setUserName(account.getUser().getFirstName() + " " + account.getUser().getLastName());
        dto.setAccountType(account.getAccountType());
        dto.setStatus(account.getStatus());
        dto.setCurrency(account.getCurrency());
        dto.setTier(account.getTier());
        dto.setBalance(account.getBalance());
        dto.setAvailableBalance(account.getAvailableBalance());
        dto.setOverdraftLimit(account.getOverdraftLimit());
        dto.setInterestRate(account.getInterestRate());
        dto.setInterestRateType(account.getInterestRateType());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setLastActivityAt(account.getLastActivityAt());
        return dto;
    }
}