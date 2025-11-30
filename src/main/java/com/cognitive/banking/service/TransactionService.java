// src/main/java/com/cognitive/banking/service/TransactionService.java
package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.Account;
import com.cognitive.banking.domain.entity.Card;
import com.cognitive.banking.domain.entity.Transaction;
import com.cognitive.banking.domain.enums.TransactionStatus;
import com.cognitive.banking.domain.enums.TransactionType;
import com.cognitive.banking.dto.CreateTransactionRequest;
import com.cognitive.banking.dto.TransactionDTO;
import com.cognitive.banking.dto.TransferRequest;
import com.cognitive.banking.repository.AccountRepository;
import com.cognitive.banking.repository.CardRepository;
import com.cognitive.banking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    public TransactionDTO createTransaction(CreateTransactionRequest request) {
        System.out.println("Creating new transaction of type: " + request.getTransactionType());

        // Validate from account exists
        Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("From account not found with ID: " + request.getFromAccountId()));

        // Validate card if provided
        Card card = null;
        if (request.getCardId() != null) {
            card = cardRepository.findById(request.getCardId())
                    .orElseThrow(() -> new RuntimeException("Card not found with ID: " + request.getCardId()));

            // Validate card belongs to account
            if (!card.getAccount().getAccountId().equals(fromAccount.getAccountId())) {
                throw new RuntimeException("Card does not belong to the specified account");
            }
        }

        // Validate to account for transfers
        Account toAccount = null;
        if (request.getToAccountId() != null) {
            toAccount = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new RuntimeException("To account not found with ID: " + request.getToAccountId()));
        }

        // Generate unique transaction reference
        String transactionReference = generateTransactionReference();

        // Create transaction entity
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(transactionReference);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setDescription(request.getDescription());
        transaction.setMerchantName(request.getMerchantName());
        transaction.setMerchantCategory(request.getMerchantCategory());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setCard(card);

        // Process transaction based on type
        Transaction processedTransaction = processTransaction(transaction);

        Transaction savedTransaction = transactionRepository.save(processedTransaction);
        System.out.println("Transaction created successfully with reference: " + savedTransaction.getTransactionReference());

        return convertToDTO(savedTransaction);
    }

    public TransactionDTO processTransfer(TransferRequest request) {
        System.out.println("Processing transfer from account: " + request.getFromAccountId() + " to account: " + request.getToAccountId());

        // Validate accounts
        Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("From account not found with ID: " + request.getFromAccountId()));

        Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new RuntimeException("To account not found with ID: " + request.getToAccountId()));

        // Check if accounts are the same
        if (fromAccount.getAccountId().equals(toAccount.getAccountId())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        // Check currency compatibility
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new RuntimeException("Currency mismatch between accounts");
        }

        // Check sufficient funds
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds in from account");
        }

        // Generate transaction reference
        String transactionReference = generateTransactionReference();

        // Create transfer transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(transactionReference);
        transaction.setTransactionType(TransactionType.TRANSFER_OUT);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setDescription(request.getDescription());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);

        // Process the transfer
        try {
            // Debit from account
            fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
            accountRepository.save(fromAccount);
            transaction.setBalanceAfter(fromAccount.getBalance());

            // Credit to account
            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
            accountRepository.save(toAccount);

            // Update transaction status
            transaction.setTransactionStatus(TransactionStatus.COMPLETED);
            transaction.setPostedDate(LocalDateTime.now());

            Transaction savedTransaction = transactionRepository.save(transaction);
            System.out.println("Transfer completed successfully");

            return convertToDTO(savedTransaction);

        } catch (Exception e) {
            // Rollback transaction
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new RuntimeException("Transfer failed: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Optional<TransactionDTO> getTransactionById(UUID transactionId) {
        System.out.println("Fetching transaction by ID: " + transactionId);
        return transactionRepository.findById(transactionId)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<TransactionDTO> getTransactionByReference(String transactionReference) {
        System.out.println("Fetching transaction by reference: " + transactionReference);
        return transactionRepository.findByTransactionReference(transactionReference)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByAccountId(UUID accountId) {
        System.out.println("Fetching transactions for account ID: " + accountId);
        return transactionRepository.findByAccountId(accountId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByCardId(UUID cardId) {
        System.out.println("Fetching transactions for card ID: " + cardId);
        return transactionRepository.findByCardCardId(cardId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByDateRange(UUID accountId, LocalDateTime startDate, LocalDateTime endDate) {
        System.out.println("Fetching transactions for account ID: " + accountId + " between " + startDate + " and " + endDate);
        return transactionRepository.findByAccountIdAndDateRange(accountId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactions() {
        System.out.println("Fetching all transactions");
        return transactionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO updateTransactionStatus(UUID transactionId, TransactionStatus status) {
        System.out.println("Updating transaction status to " + status + " for transaction ID: " + transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));

        transaction.setTransactionStatus(status);

        if (status == TransactionStatus.COMPLETED) {
            transaction.setPostedDate(LocalDateTime.now());
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);
        System.out.println("Transaction status updated successfully");

        return convertToDTO(updatedTransaction);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        return account.getBalance();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalDebits(UUID accountId, LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalDebits = transactionRepository.getTotalDebitsByAccountIdAndDateRange(accountId, startDate, endDate);
        return totalDebits != null ? totalDebits : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCredits(UUID accountId, LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalCredits = transactionRepository.getTotalCreditsByAccountIdAndDateRange(accountId, startDate, endDate);
        return totalCredits != null ? totalCredits : BigDecimal.ZERO;
    }

    private Transaction processTransaction(Transaction transaction) {
        Account account = transaction.getFromAccount();

        switch (transaction.getTransactionType()) {
            case PURCHASE:
            case ATM_WITHDRAWAL:
            case TRANSFER_OUT:
            case FEE:
                // Debit transactions
                if (account.getBalance().compareTo(transaction.getAmount()) < 0) {
                    transaction.setTransactionStatus(TransactionStatus.DECLINED);
                    throw new RuntimeException("Insufficient funds for transaction");
                }
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
                transaction.setBalanceAfter(account.getBalance());
                transaction.setTransactionStatus(TransactionStatus.COMPLETED);
                transaction.setPostedDate(LocalDateTime.now());
                break;

            case DEPOSIT:
            case TRANSFER_IN:
            case REFUND:
            case INTEREST:
                // Credit transactions
                account.setBalance(account.getBalance().add(transaction.getAmount()));
                transaction.setBalanceAfter(account.getBalance());
                transaction.setTransactionStatus(TransactionStatus.COMPLETED);
                transaction.setPostedDate(LocalDateTime.now());
                break;

            default:
                transaction.setTransactionStatus(TransactionStatus.PENDING);
                break;
        }

        accountRepository.save(account);
        return transaction;
    }

    private String generateTransactionReference() {
        String reference;
        do {
            // Generate 12-character alphanumeric reference
            reference = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();
        } while (transactionRepository.findByTransactionReference(reference).isPresent());

        return reference;
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        String fromAccountNumber = transaction.getFromAccount() != null ?
                transaction.getFromAccount().getAccountNumber() : null;
        String toAccountNumber = transaction.getToAccount() != null ?
                transaction.getToAccount().getAccountNumber() : null;
        String cardNumber = transaction.getCard() != null ?
                maskCardNumber(transaction.getCard().getCardNumber()) : null;

        UUID fromAccountId = transaction.getFromAccount() != null ?
                transaction.getFromAccount().getAccountId() : null;
        UUID toAccountId = transaction.getToAccount() != null ?
                transaction.getToAccount().getAccountId() : null;
        UUID cardId = transaction.getCard() != null ?
                transaction.getCard().getCardId() : null;

        return new TransactionDTO(
                transaction.getTransactionId(),
                transaction.getTransactionReference(),
                transaction.getTransactionType(),
                transaction.getTransactionStatus(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getMerchantName(),
                transaction.getMerchantCategory(),
                fromAccountId,
                fromAccountNumber,
                toAccountId,
                toAccountNumber,
                cardId,
                cardNumber,
                transaction.getBalanceAfter(),
                transaction.getAvailableBalanceAfter(),
                transaction.getTransactionDate(),
                transaction.getPostedDate(),
                transaction.getCreatedAt()
        );
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 12) {
            return cardNumber;
        }
        String firstFour = cardNumber.substring(0, 4);
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return firstFour + "********" + lastFour;
    }
}