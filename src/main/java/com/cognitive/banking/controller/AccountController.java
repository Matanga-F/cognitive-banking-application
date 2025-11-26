// src/main/java/com/cognitive/banking/controller/AccountController.java
package com.cognitive.banking.controller;

import com.cognitive.banking.dto.AccountDTO;
import com.cognitive.banking.dto.CreateAccountRequest;
import com.cognitive.banking.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/accounts")  // ✅ Changed from "/api/accounts" to "/accounts"
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@RequestBody CreateAccountRequest request) {
        AccountDTO account = accountService.createAccount(request);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable String accountNumber) {
        AccountDTO account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountDTO>> getUserAccounts(@PathVariable Long userId) {
        List<AccountDTO> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable String accountNumber) {
        BigDecimal balance = accountService.getAccountBalance(accountNumber);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<AccountDTO> deposit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        AccountDTO account = accountService.deposit(accountNumber, amount);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<AccountDTO> withdraw(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        AccountDTO account = accountService.withdraw(accountNumber, amount);
        return ResponseEntity.ok(account);
    }
    // Add this method to src/main/java/com/cognitive/banking/controller/AccountController.java
    @PostMapping("/transfer")
    public ResponseEntity<AccountDTO> transfer(
            @RequestParam String fromAccountNumber,
            @RequestParam String toAccountNumber,
            @RequestParam BigDecimal amount) {
        AccountDTO account = accountService.transfer(fromAccountNumber, toAccountNumber, amount);
        return ResponseEntity.ok(account);
    }
}