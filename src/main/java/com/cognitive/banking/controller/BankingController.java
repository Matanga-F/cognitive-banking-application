// src/main/java/com/cognitive/banking/controller/BankingController.java
package com.cognitive.banking.controller;

import com.cognitive.banking.dto.*;
import com.cognitive.banking.service.AccountService;
import com.cognitive.banking.service.TransactionService;
import com.cognitive.banking.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/banking")
public class BankingController {
    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public BankingController(UserService userService, AccountService accountService, TransactionService transactionService) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    // System Status
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "OPERATIONAL",
                "message", "🚀 Cognitive Banking System is RUNNING!",
                "version", "1.0.0",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    // Quick User Creation (for testing)
    @PostMapping("/quick-user")
    public ResponseEntity<UserDTO> createQuickUser() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Test");
        request.setLastName("User");
        request.setUsername("testuser");
        request.setEmail("test@bank.com");
        request.setPhoneNumber("1234567890");
        request.setPassword("password123");

        UserDTO user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }

    // Quick Account Creation (for testing)
    @PostMapping("/quick-account/{userId}")
    public ResponseEntity<AccountDTO> createQuickAccount(@PathVariable Long userId) {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUserId(userId);
        request.setAccountType(com.cognitive.banking.domain.enums.AccountType.SAVINGS);
        request.setCurrency(com.cognitive.banking.domain.enums.Currency.ZAR);
        request.setInitialDeposit(new BigDecimal("1000.00"));

        AccountDTO account = accountService.createAccount(request);
        return ResponseEntity.ok(account);
    }

    // Dashboard Summary
    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable Long userId) {
        List<AccountDTO> accounts = accountService.getUserAccounts(userId);
        List<TransactionDTO> recentTransactions = transactionService.getUserTransactions(userId);

        BigDecimal totalBalance = BigDecimal.ZERO;
        for (AccountDTO account : accounts) {
            totalBalance = totalBalance.add(account.getBalance());
        }

        return ResponseEntity.ok(Map.of(
                "totalAccounts", accounts.size(),
                "totalBalance", totalBalance,
                "recentTransactions", recentTransactions.stream().limit(5).toList(),
                "accounts", accounts
        ));
    }
}