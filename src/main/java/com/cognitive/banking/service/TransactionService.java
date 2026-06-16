package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.Account;
import com.cognitive.banking.domain.entity.Card;
import com.cognitive.banking.domain.entity.Transaction;
import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.TransactionStatus;
import com.cognitive.banking.domain.enums.TransactionType;
import com.cognitive.banking.dto.CreateTransactionRequest;
import com.cognitive.banking.dto.TransactionDTO;
import com.cognitive.banking.dto.TransferRequest;
import com.cognitive.banking.repository.AccountRepository;
import com.cognitive.banking.repository.CardRepository;
import com.cognitive.banking.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final EmailService emailService;
    private final PhoneService phoneService;

    private static final String TRANSACTION_CACHE = "transactions";

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              CardRepository cardRepository,
                              EmailService emailService,
                              PhoneService phoneService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.emailService = emailService;
        this.phoneService = phoneService;
    }

    // ============================
    // CREATE TRANSACTION
    // ============================

    public TransactionDTO createTransaction(CreateTransactionRequest request) {
        logger.info("Creating new transaction of type: {}", request.getTransactionType());

        // Validate from account
        Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("From account not found with ID: " + request.getFromAccountId()));

        // Validate card if provided
        Card card = null;
        if (request.getCardId() != null) {
            card = cardRepository.findById(request.getCardId())
                    .orElseThrow(() -> new RuntimeException("Card not found with ID: " + request.getCardId()));
            if (!card.getAccount().getAccountId().equals(fromAccount.getAccountId())) {
                throw new RuntimeException("Card does not belong to the specified account");
            }
            // Check if card is active
            if (card.getCardStatus() != com.cognitive.banking.domain.enums.CardStatus.ACTIVE) {
                throw new RuntimeException("Card is not active");
            }
            // Check daily limit
            if (card.getDailyLimit() != null && request.getAmount().compareTo(card.getDailyLimit()) > 0) {
                throw new RuntimeException("Transaction amount exceeds daily limit");
            }
        }

        // Validate to account for transfers
        Account toAccount = null;
        if (request.getToAccountId() != null) {
            toAccount = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new RuntimeException("To account not found with ID: " + request.getToAccountId()));
        }

        String transactionReference = generateTransactionReference();

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
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionStatus(TransactionStatus.PENDING);

        Transaction processedTransaction = processTransaction(transaction);
        Transaction savedTransaction = transactionRepository.save(processedTransaction);

        // Send notifications
        sendTransactionNotifications(savedTransaction);

        logger.info("Transaction created successfully with reference: {}", savedTransaction.getTransactionReference());
        return convertToDTO(savedTransaction);
    }

    // ============================
    // PROCESS TRANSFER
    // ============================

    public TransactionDTO processTransfer(TransferRequest request) {
        logger.info("Processing transfer from account: {} to account: {}",
                maskAccountNumber(request.getFromAccountId()), maskAccountNumber(request.getToAccountId()));

        // Validate accounts
        Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("From account not found"));

        Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new RuntimeException("To account not found"));

        // Validation checks
        if (fromAccount.getAccountId().equals(toAccount.getAccountId())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new RuntimeException("Currency mismatch between accounts");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be greater than zero");
        }

        String transactionReference = generateTransactionReference();

        Transaction transaction = new Transaction();
        transaction.setTransactionReference(transactionReference);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(fromAccount.getCurrency());
        transaction.setDescription(request.getDescription());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setTransactionDate(LocalDateTime.now());

        try {
            // Debit from account
            fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
            accountRepository.save(fromAccount);
            transaction.setBalanceAfter(fromAccount.getBalance());

            // Credit to account
            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
            accountRepository.save(toAccount);

            transaction.setTransactionStatus(TransactionStatus.COMPLETED);
            transaction.setPostedDate(LocalDateTime.now());

            Transaction savedTransaction = transactionRepository.save(transaction);

            // Send notifications
            sendTransferNotifications(savedTransaction, fromAccount.getUser(), toAccount.getUser());

            logger.info("Transfer completed successfully: {}", transactionReference);
            return convertToDTO(savedTransaction);

        } catch (Exception e) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            logger.error("Transfer failed: {}", e.getMessage());
            throw new RuntimeException("Transfer failed: " + e.getMessage());
        }
    }

    // ============================
    // GET TRANSACTIONS
    // ============================

    @Cacheable(value = TRANSACTION_CACHE, key = "#transactionId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<TransactionDTO> getTransactionById(UUID transactionId) {
        logger.debug("Fetching transaction by ID: {}", transactionId);
        return transactionRepository.findById(transactionId).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<TransactionDTO> getTransactionByReference(String transactionReference) {
        logger.debug("Fetching transaction by reference: {}", transactionReference);
        return transactionRepository.findByTransactionReference(transactionReference).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByAccountId(UUID accountId) {
        logger.debug("Fetching transactions for account ID: {}", accountId);
        return transactionRepository.findByAccountId(accountId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByCardId(UUID cardId) {
        logger.debug("Fetching transactions for card ID: {}", cardId);
        return transactionRepository.findByCardCardId(cardId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByDateRange(UUID accountId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching transactions for account {} between {} and {}", accountId, startDate, endDate);
        return transactionRepository.findByAccountIdAndDateRange(accountId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByType(UUID accountId, TransactionType type) {
        logger.debug("Fetching {} transactions for account ID: {}", type, accountId);
        return transactionRepository.findByFromAccountAccountIdAndTransactionType(accountId, type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getRecentTransactions(UUID accountId, int limit) {
        logger.debug("Fetching last {} transactions for account ID: {}", limit, accountId);
        return transactionRepository.findTopNByAccountId(accountId, limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactions() {
        logger.info("Fetching all transactions");
        return transactionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============================
    // UPDATE TRANSACTION
    // ============================

    @CacheEvict(value = TRANSACTION_CACHE, key = "#transactionId")
    public TransactionDTO updateTransactionStatus(UUID transactionId, TransactionStatus status) {
        logger.info("Updating transaction status to {} for transaction ID: {}", status, transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setTransactionStatus(status);
        if (status == TransactionStatus.COMPLETED) {
            transaction.setPostedDate(LocalDateTime.now());
        }
        transaction.setTransactionDate(LocalDateTime.now());

        Transaction updatedTransaction = transactionRepository.save(transaction);
        logger.info("Transaction status updated successfully for: {}", transaction.getTransactionReference());

        return convertToDTO(updatedTransaction);
    }

    @Transactional(readOnly = true)
    public boolean isAccountOwnedByUser(UUID accountId, UUID userId) {
        return accountRepository.existsByAccountIdAndUserUserId(accountId, userId);
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByUserId(UUID userId) {
        logger.debug("Fetching transactions for user ID: {}", userId);
        return transactionRepository.findByUserUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============================
    // BALANCE QUERIES
    // ============================

    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return account.getBalance();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalSpentToday(UUID accountId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        BigDecimal totalSpent = transactionRepository.getTotalAmountByAccountIdAndDateRange(
                accountId, startOfDay, LocalDateTime.now(), TransactionType.PURCHASE);
        return totalSpent != null ? totalSpent : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalDepositsThisMonth(UUID accountId) {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        BigDecimal totalDeposits = transactionRepository.getTotalAmountByAccountIdAndDateRange(
                accountId, startOfMonth, LocalDateTime.now(), TransactionType.DEPOSIT);
        return totalDeposits != null ? totalDeposits : BigDecimal.ZERO;
    }

    // ============================
    // REVERSE TRANSACTION
    // ============================

    @Transactional
    public TransactionDTO reverseTransaction(UUID transactionId, String reason) {
        logger.info("Reversing transaction with ID: {}", transactionId);

        Transaction originalTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (originalTransaction.getTransactionStatus() != TransactionStatus.COMPLETED) {
            throw new RuntimeException("Only completed transactions can be reversed");
        }

        if (originalTransaction.getPostedDate() != null &&
                originalTransaction.getPostedDate().plusDays(30).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot reverse transactions older than 30 days");
        }

        // Create reversal transaction
        Transaction reversalTransaction = new Transaction();
        reversalTransaction.setTransactionReference(generateTransactionReference());
        reversalTransaction.setTransactionType(TransactionType.REFUND);
        reversalTransaction.setAmount(originalTransaction.getAmount());
        reversalTransaction.setCurrency(originalTransaction.getCurrency());
        reversalTransaction.setDescription("REVERSAL: " + reason + " - Original Ref: " + originalTransaction.getTransactionReference());
        reversalTransaction.setFromAccount(originalTransaction.getToAccount());
        reversalTransaction.setToAccount(originalTransaction.getFromAccount());
        reversalTransaction.setTransactionDate(LocalDateTime.now());

        // Process reversal
        Account toAccount = originalTransaction.getFromAccount();
        Account fromAccount = originalTransaction.getToAccount();

        if (toAccount != null) {
            toAccount.setBalance(toAccount.getBalance().add(originalTransaction.getAmount()));
            accountRepository.save(toAccount);
            reversalTransaction.setBalanceAfter(toAccount.getBalance());
        }

        reversalTransaction.setTransactionStatus(TransactionStatus.COMPLETED);
        reversalTransaction.setPostedDate(LocalDateTime.now());

        Transaction savedReversal = transactionRepository.save(reversalTransaction);

        // Update original transaction
        originalTransaction.setTransactionStatus(TransactionStatus.REVERSED);
        transactionRepository.save(originalTransaction);

        logger.info("Transaction reversed successfully. Reversal ref: {}", savedReversal.getTransactionReference());

        return convertToDTO(savedReversal);
    }

    // ============================
    // PRIVATE HELPER METHODS
    // ============================

    private Transaction processTransaction(Transaction transaction) {
        Account account = transaction.getFromAccount();

        switch (transaction.getTransactionType()) {
            case PURCHASE:
            case ATM_WITHDRAWAL:
            case TRANSFER:
            case FEE:
                // Debit transactions
                if (account.getBalance().compareTo(transaction.getAmount()) < 0) {
                    transaction.setTransactionStatus(TransactionStatus.DECLINED);
                    throw new RuntimeException("Insufficient funds");
                }
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
                transaction.setBalanceAfter(account.getBalance());
                transaction.setTransactionStatus(TransactionStatus.COMPLETED);
                transaction.setPostedDate(LocalDateTime.now());
                break;

            case DEPOSIT:
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

    private void sendTransactionNotifications(Transaction transaction) {
        if (transaction.getFromAccount() != null && transaction.getFromAccount().getUser() != null) {
            User user = transaction.getFromAccount().getUser();
            String type = transaction.getTransactionType().name();
            String amount = transaction.getCurrency() + " " + transaction.getAmount();
            String accountNumber = transaction.getFromAccount().getAccountNumber();

            try {
                emailService.sendTransactionAlert(user.getEmail(), type, amount, accountNumber);
                phoneService.sendTransactionAlert(user.getPhoneNumber(), type, amount, accountNumber);
            } catch (Exception e) {
                logger.warn("Failed to send transaction notifications: {}", e.getMessage());
            }
        }
    }

    private void sendTransferNotifications(Transaction transaction, User fromUser, User toUser) {
        String amount = transaction.getCurrency() + " " + transaction.getAmount();
        String fromAccountNumber = transaction.getFromAccount().getAccountNumber();
        String toAccountNumber = transaction.getToAccount().getAccountNumber();

        try {
            // Notify sender
            emailService.sendTransactionAlert(fromUser.getEmail(), "DEBIT", amount, fromAccountNumber);
            phoneService.sendTransactionAlert(fromUser.getPhoneNumber(), "DEBIT", amount, fromAccountNumber);

            // Notify receiver
            emailService.sendTransactionAlert(toUser.getEmail(), "CREDIT", amount, toAccountNumber);
            phoneService.sendTransactionAlert(toUser.getPhoneNumber(), "CREDIT", amount, toAccountNumber);
        } catch (Exception e) {
            logger.warn("Failed to send transfer notifications: {}", e.getMessage());
        }
    }

    private String generateTransactionReference() {
        String reference;
        do {
            reference = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();
        } while (transactionRepository.findByTransactionReference(reference).isPresent());
        return reference;
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) return accountNumber;
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    private String maskAccountNumber(UUID accountId) {
        return accountId != null ? accountId.toString().substring(0, 8) + "..." : "unknown";
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 12) return cardNumber;
        return cardNumber.substring(0, 4) + "********" + cardNumber.substring(cardNumber.length() - 4);
    }
    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setTransactionReference(transaction.getTransactionReference());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setTransactionStatus(transaction.getTransactionStatus());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setDescription(transaction.getDescription());
        dto.setMerchantName(transaction.getMerchantName());
        dto.setMerchantCategory(transaction.getMerchantCategory());
        dto.setFromAccountId(transaction.getFromAccount() != null ? transaction.getFromAccount().getAccountId() : null);
        dto.setFromAccountNumber(transaction.getFromAccount() != null ? maskAccountNumber(transaction.getFromAccount().getAccountNumber()) : null);
        dto.setToAccountId(transaction.getToAccount() != null ? transaction.getToAccount().getAccountId() : null);
        dto.setToAccountNumber(transaction.getToAccount() != null ? maskAccountNumber(transaction.getToAccount().getAccountNumber()) : null);
        dto.setCardId(transaction.getCard() != null ? transaction.getCard().getCardId() : null);
        dto.setCardNumber(transaction.getCard() != null ? maskCardNumber(transaction.getCard().getCardNumber()) : null);
        dto.setBalanceAfter(transaction.getBalanceAfter());
        dto.setAvailableBalanceAfter(transaction.getAvailableBalanceAfter());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setPostedDate(transaction.getPostedDate());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
}