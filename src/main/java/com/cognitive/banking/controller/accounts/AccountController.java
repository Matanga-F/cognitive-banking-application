package com.cognitive.banking.controller.accounts;

import com.cognitive.banking.annotation.RequiresPermission;
import com.cognitive.banking.annotation.RequiresRole;
import com.cognitive.banking.domain.enums.AccountStatus;
import com.cognitive.banking.domain.enums.UserRole;
import com.cognitive.banking.dto.AccountDTO;
import com.cognitive.banking.dto.CreateAccountRequest;
import com.cognitive.banking.dto.UpdateAccountRequest;
import com.cognitive.banking.monitoring.metrics.BankingMetrics;
import com.cognitive.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Account Management", description = "Endpoints for managing bank accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private BankingMetrics bankingMetrics;

    // ============================================
    // CREATE ACCOUNT - Requires authentication
    // ============================================
    @PostMapping
    @RequiresPermission("account:create")
    @Operation(summary = "Create a new bank account",
            description = "Creates a new account for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        logger.info("REST request to create account for user: {}", request.getUserId());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        // Verify user can only create accounts for themselves unless admin
        if (!isAdmin() && !currentUser.equals(getUserEmailFromId(request.getUserId()))) {
            logger.warn("User {} attempted to create account for another user: {}",
                    currentUser, request.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        AccountDTO result = accountService.createAccount(request);
        bankingMetrics.recordAccountCreated(
                result.getAccountType().name(),
                result.getCurrency(),
                result.getBalance()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ============================================
    // GET ACCOUNT BY ID - Requires authentication
    // ============================================
    @GetMapping("/{accountId}")
    @RequiresPermission("account:read")
    @Operation(summary = "Get account by ID", description = "Retrieves account details by account ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AccountDTO> getAccountById(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId) {

        logger.debug("REST request to get account by ID: {}", accountId);
        bankingMetrics.recordAccountQuery("by_id");

        return accountService.getAccountById(accountId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================
    // GET ACCOUNT BY NUMBER - Requires authentication
    // ============================================
    @GetMapping("/number/{accountNumber}")
    @RequiresPermission("account:read")
    @Operation(summary = "Get account by account number", description = "Retrieves account details by account number")
    public ResponseEntity<AccountDTO> getAccountByNumber(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber) {

        logger.debug("REST request to get account by number");
        bankingMetrics.recordAccountQuery("by_number");

        return accountService.getAccountByNumber(accountNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================
    // GET ACCOUNTS BY USER ID - Role based access
    // ============================================
    @GetMapping("/user/{userId}")
    @RequiresPermission("account:read")
    @Operation(summary = "Get accounts by user ID", description = "Retrieves all accounts for a specific user")
    public ResponseEntity<List<AccountDTO>> getAccountsByUserId(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId) {

        logger.info("REST request to get accounts for user: {}", userId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        // Users can only view their own accounts unless admin
        if (!isAdmin() && !currentUser.equals(getUserEmailFromId(userId))) {
            logger.warn("User {} attempted to view accounts of user: {}", currentUser, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        bankingMetrics.recordAccountQuery("by_user_id");
        List<AccountDTO> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    // ============================================
    // GET MY ACCOUNTS - Convenience for current user
    // ============================================
    @GetMapping("/my-accounts")
    @RequiresPermission("account:read")
    @Operation(summary = "Get my accounts", description = "Retrieves all accounts for the currently authenticated user")
    public ResponseEntity<List<AccountDTO>> getMyAccounts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        logger.info("REST request to get accounts for current user: {}", email);

        // Get user ID from email (you'll need to implement this lookup)
        UUID userId = getUserIdFromEmail(email);

        bankingMetrics.recordAccountQuery("my_accounts");
        List<AccountDTO> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    // ============================================
    // GET ALL ACCOUNTS - Admin only
    // ============================================
    @GetMapping("/all")
    @RequiresRole(UserRole.ADMIN)
    @RequiresPermission("account:read")
    @Operation(summary = "Get all accounts", description = "Retrieves all accounts in the system (Admin only)")
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        logger.info("REST request to get all accounts (Admin)");
        bankingMetrics.recordAccountQuery("all");

        List<AccountDTO> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    // ============================================
    // UPDATE ACCOUNT - Requires authentication
    // ============================================
    @PutMapping("/{accountId}")
    @RequiresPermission("account:update")
    @Operation(summary = "Update account", description = "Updates account details")
    public ResponseEntity<AccountDTO> updateAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateAccountRequest request) {

        logger.info("REST request to update account: {}", accountId);

        // Verify ownership or admin
        if (!canAccessAccount(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        AccountDTO result = accountService.updateAccount(accountId, request);
        bankingMetrics.recordAccountUpdate(result.getAccountType().name());

        return ResponseEntity.ok(result);
    }

    // ============================================
    // UPDATE ACCOUNT STATUS - Admin only
    // ============================================
    @PatchMapping("/{accountId}/status")
    @RequiresRole(UserRole.ADMIN)
    @RequiresPermission("account:update")
    @Operation(summary = "Update account status", description = "Updates the status of an account (Admin only)")
    public ResponseEntity<AccountDTO> updateAccountStatus(
            @PathVariable UUID accountId,
            @RequestParam AccountStatus status) {

        logger.info("REST request to update status for account: {} to {}", accountId, status);

        bankingMetrics.recordAccountStatusChange("CURRENT", status.name());
        AccountDTO result = accountService.updateAccountStatus(accountId, status);

        return ResponseEntity.ok(result);
    }

    // ============================================
    // CLOSE ACCOUNT - Requires authentication
    // ============================================
    @DeleteMapping("/{accountId}")
    @RequiresPermission("account:delete")
    @Operation(summary = "Close account", description = "Closes an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account closed successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Void> closeAccount(@PathVariable UUID accountId) {
        logger.info("REST request to close account: {}", accountId);

        // Verify ownership or admin
        if (!canAccessAccount(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        accountService.closeAccount(accountId);
        bankingMetrics.recordAccountClosed("CLOSED");

        return ResponseEntity.noContent().build();
    }

    // ============================================
    // GET ACCOUNT BALANCE - Requires authentication
    // ============================================
    @GetMapping("/{accountId}/balance")
    @RequiresPermission("account:read")
    @Operation(summary = "Get account balance", description = "Retrieves the current balance of an account")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable UUID accountId) {
        logger.debug("REST request to get balance for account: {}", accountId);

        if (!canAccessAccount(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        bankingMetrics.recordBalanceCheck();
        BigDecimal balance = accountService.getAccountBalance(accountId);
        bankingMetrics.recordBalanceAmount(balance);

        return ResponseEntity.ok(balance);
    }

    // ============================================
    // GET TOTAL BALANCE - For current user
    // ============================================
    @GetMapping("/my-balance")
    @RequiresPermission("account:read")
    @Operation(summary = "Get my total balance", description = "Retrieves total balance across all user accounts")
    public ResponseEntity<BigDecimal> getMyTotalBalance() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        logger.info("REST request to get total balance for user: {}", email);

        UUID userId = getUserIdFromEmail(email);
        bankingMetrics.recordTotalBalanceCheck(userId, null);

        BigDecimal totalBalance = accountService.getTotalBalanceByUserId(userId);
        return ResponseEntity.ok(totalBalance);
    }

    // ============================================
    // GET ACTIVE ACCOUNTS COUNT - For current user
    // ============================================
    @GetMapping("/my-active-count")
    @RequiresPermission("account:read")
    @Operation(summary = "Get my active accounts count", description = "Retrieves count of active accounts for current user")
    public ResponseEntity<Long> getMyActiveAccountsCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        UUID userId = getUserIdFromEmail(email);
        long count = accountService.getActiveAccountsCountByUserId(userId);

        return ResponseEntity.ok(count);
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

        return accountService.getAccountById(accountId)
                .map(account -> account.getUserEmail() != null &&
                        account.getUserEmail().equals(currentUserEmail))
                .orElse(false);
    }

    private UUID getUserIdFromEmail(String email) {
        // You'll need to inject UserRepository or create a UserService method
        // For now, return a placeholder - implement this properly
        return accountService.getUserIdByEmail(email);
    }

    private String getUserEmailFromId(UUID userId) {
        // You'll need to implement this
        return accountService.getUserEmailById(userId);
    }
}