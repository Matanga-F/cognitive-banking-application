package com.cognitive.banking.controller.transactions;

import com.cognitive.banking.annotation.RequiresPermission;
import com.cognitive.banking.annotation.RequiresRole;
import com.cognitive.banking.domain.enums.TransactionStatus;
import com.cognitive.banking.domain.enums.TransactionType;
import com.cognitive.banking.domain.enums.UserRole;
import com.cognitive.banking.dto.CreateTransactionRequest;
import com.cognitive.banking.dto.TransactionDTO;
import com.cognitive.banking.dto.TransferRequest;
import com.cognitive.banking.service.TransactionService;
import com.cognitive.banking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transaction Management", description = "Endpoints for managing financial transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private UserService userService;

    // ============================================
    // CREATE TRANSACTION
    // ============================================
    @PostMapping
    @RequiresPermission("transaction:create")
    @Operation(summary = "Create a new transaction", description = "Creates a new transaction (purchase, deposit, withdrawal, etc.)")
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        logger.info("REST request to create transaction: {}", request.getTransactionType());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        // Verify user has access to the from account
        if (!canAccessAccount(request.getFromAccountId())) {
            logger.warn("User {} attempted to create transaction for account: {}", currentUser, request.getFromAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TransactionDTO result = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ============================================
    // PROCESS TRANSFER
    // ============================================
    @PostMapping("/transfer")
    @RequiresPermission("transaction:create")
    @Operation(summary = "Process transfer", description = "Transfers money between accounts")
    public ResponseEntity<TransactionDTO> processTransfer(@Valid @RequestBody TransferRequest request) {
        logger.info("REST request to process transfer from: {} to: {}",
                request.getFromAccountId(), request.getToAccountId());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        // Verify user has access to both accounts
        if (!canAccessAccount(request.getFromAccountId()) || !canAccessAccount(request.getToAccountId())) {
            logger.warn("User {} attempted unauthorized transfer", currentUser);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TransactionDTO result = transactionService.processTransfer(request);
        return ResponseEntity.ok(result);
    }

    // ============================================
    // GET TRANSACTION BY ID
    // ============================================
    @GetMapping("/{transactionId}")
    @RequiresPermission("transaction:read")
    @Operation(summary = "Get transaction by ID", description = "Retrieves transaction details by ID")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable UUID transactionId) {
        logger.debug("REST request to get transaction by ID: {}", transactionId);

        return transactionService.getTransactionById(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================
    // GET TRANSACTION BY REFERENCE
    // ============================================
    @GetMapping("/reference/{transactionReference}")
    @RequiresPermission("transaction:read")
    @Operation(summary = "Get transaction by reference", description = "Retrieves transaction details by reference number")
    public ResponseEntity<TransactionDTO> getTransactionByReference(@PathVariable String transactionReference) {
        logger.debug("REST request to get transaction by reference: {}", transactionReference);

        return transactionService.getTransactionByReference(transactionReference)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================
    // GET TRANSACTIONS BY ACCOUNT
    // ============================================
    @GetMapping("/account/{accountId}")
    @RequiresPermission("transaction:read")
    @Operation(summary = "Get transactions by account", description = "Retrieves all transactions for a specific account")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccountId(@PathVariable UUID accountId) {
        logger.info("REST request to get transactions for account: {}", accountId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        if (!canAccessAccount(accountId)) {
            logger.warn("User {} attempted to view transactions for account: {}", currentUser, accountId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<TransactionDTO> transactions = transactionService.getTransactionsByAccountId(accountId);
        return ResponseEntity.ok(transactions);
    }

    // ============================================
    // GET MY TRANSACTIONS
    // ============================================
    @GetMapping("/my-transactions")
    @RequiresPermission("transaction:read")
    @Operation(summary = "Get my transactions", description = "Retrieves all transactions for the current user's accounts")
    public ResponseEntity<List<TransactionDTO>> getMyTransactions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        logger.info("REST request to get transactions for current user: {}", email);

        UUID userId = getUserIdFromEmail(email);
        List<TransactionDTO> transactions = transactionService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    // ============================================
    // GET TRANSACTIONS BY CARD
    // ============================================
    @GetMapping("/card/{cardId}")
    @RequiresPermission("transaction:read")
    @Operation(summary = "Get transactions by card", description = "Retrieves all transactions for a specific card")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByCardId(@PathVariable UUID cardId) {
        logger.debug("REST request to get transactions for card: {}", cardId);

        List<TransactionDTO> transactions = transactionService.getTransactionsByCardId(cardId);
        return ResponseEntity.ok(transactions);
    }

    // ============================================
    // GET TRANSACTIONS BY DATE RANGE
    // ============================================
    @GetMapping("/account/{accountId}/date-range")
    @RequiresPermission("transaction:read")
    @Operation(summary = "Get transactions by date range", description = "Retrieves transactions within a date range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @PathVariable UUID accountId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        logger.info("REST request to get transactions for account {} between {} and {}", accountId, startDate, endDate);

        if (!canAccessAccount(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<TransactionDTO> transactions = transactionService.getTransactionsByDateRange(accountId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    // ============================================
    // GET TRANSACTIONS BY TYPE
    // ============================================
    @GetMapping("/account/{accountId}/type/{type}")
    @RequiresPermission("transaction:read")
    @Operation(summary = "Get transactions by type", description = "Retrieves transactions of a specific type")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByType(
            @PathVariable UUID accountId,
            @PathVariable TransactionType type) {
        logger.info("REST request to get {} transactions for account: {}", type, accountId);

        if (!canAccessAccount(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<TransactionDTO> transactions = transactionService.getTransactionsByType(accountId, type);
        return ResponseEntity.ok(transactions);
    }

    // ============================================
    // GET RECENT TRANSACTIONS
    // ============================================
    @GetMapping("/account/{accountId}/recent")
    @RequiresPermission("transaction:read")
    @Operation(summary = "Get recent transactions", description = "Retrieves the most recent transactions")
    public ResponseEntity<List<TransactionDTO>> getRecentTransactions(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "10") int limit) {
        logger.info("REST request to get last {} transactions for account: {}", limit, accountId);

        if (!canAccessAccount(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<TransactionDTO> transactions = transactionService.getRecentTransactions(accountId, limit);
        return ResponseEntity.ok(transactions);
    }

    // ============================================
    // GET ALL TRANSACTIONS (ADMIN ONLY)
    // ============================================
    @GetMapping("/all")
    @RequiresRole(UserRole.ADMIN)
    @RequiresPermission("transaction:read")
    @Operation(summary = "Get all transactions", description = "Retrieves all transactions in the system (Admin only)")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        logger.info("REST request to get all transactions (Admin)");
        List<TransactionDTO> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    // ============================================
    // UPDATE TRANSACTION STATUS
    // ============================================
    @PatchMapping("/{transactionId}/status")
    @RequiresRole(UserRole.ADMIN)
    @RequiresPermission("transaction:update")
    @Operation(summary = "Update transaction status", description = "Updates the status of a transaction (Admin only)")
    public ResponseEntity<TransactionDTO> updateTransactionStatus(
            @PathVariable UUID transactionId,
            @RequestParam TransactionStatus status) {
        logger.info("REST request to update transaction status to {} for: {}", status, transactionId);

        TransactionDTO result = transactionService.updateTransactionStatus(transactionId, status);
        return ResponseEntity.ok(result);
    }

    // ============================================
    // REVERSE TRANSACTION
    // ============================================
    @PostMapping("/{transactionId}/reverse")
    @RequiresPermission("transaction:update")
    @Operation(summary = "Reverse transaction", description = "Reverses a completed transaction")
    public ResponseEntity<TransactionDTO> reverseTransaction(
            @PathVariable UUID transactionId,
            @RequestParam String reason) {
        logger.info("REST request to reverse transaction: {} with reason: {}", transactionId, reason);

        TransactionDTO result = transactionService.reverseTransaction(transactionId, reason);
        return ResponseEntity.ok(result);
    }

    // ============================================
    // GET ACCOUNT BALANCE
    // ============================================
    @GetMapping("/balance/{accountId}")
    @RequiresPermission("transaction:read")
    @Operation(summary = "Get account balance", description = "Retrieves the current balance of an account")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable UUID accountId) {
        logger.debug("REST request to get balance for account: {}", accountId);

        if (!canAccessAccount(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BigDecimal balance = transactionService.getAccountBalance(accountId);
        return ResponseEntity.ok(balance);
    }

    // ============================================
    // GET TOTAL SPENT TODAY
    // ============================================
    @GetMapping("/spent-today/{accountId}")
    @RequiresPermission("transaction:read")
    @Operation(summary = "Get total spent today", description = "Retrieves total amount spent today from an account")
    public ResponseEntity<BigDecimal> getTotalSpentToday(@PathVariable UUID accountId) {
        logger.debug("REST request to get total spent today for account: {}", accountId);

        if (!canAccessAccount(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BigDecimal totalSpent = transactionService.getTotalSpentToday(accountId);
        return ResponseEntity.ok(totalSpent);
    }

    // ============================================
    // GET TOTAL DEPOSITS THIS MONTH
    // ============================================
    @GetMapping("/deposits-month/{accountId}")
    @RequiresPermission("transaction:read")
    @Operation(summary = "Get total deposits this month", description = "Retrieves total deposits for the current month")
    public ResponseEntity<BigDecimal> getTotalDepositsThisMonth(@PathVariable UUID accountId) {
        logger.debug("REST request to get total deposits this month for account: {}", accountId);

        if (!canAccessAccount(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BigDecimal totalDeposits = transactionService.getTotalDepositsThisMonth(accountId);
        return ResponseEntity.ok(totalDeposits);
    }

    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean canAccessAccount(UUID accountId) {
        if (isAdmin()) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        UUID currentUserId = getUserIdFromEmail(currentUserEmail);

        return transactionService.isAccountOwnedByUser(accountId, currentUserId);
    }

    private UUID getUserIdFromEmail(String email) {
        // You'll need to implement this based on your UserService
        // This should call userService.getUserIdByEmail(email)
        return userService.getUserIdByEmail(email);
    }
}