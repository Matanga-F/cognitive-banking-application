// src/main/java/com/cognitive/banking/controller/AccountController.java
package com.cognitive.banking.controller;

import com.cognitive.banking.domain.enums.AccountStatus;
import com.cognitive.banking.dto.AccountDTO;
import com.cognitive.banking.dto.CreateAccountRequest;
import com.cognitive.banking.dto.UpdateAccountRequest;
import com.cognitive.banking.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountDTO accountDTO = accountService.createAccount(request);
        return new ResponseEntity<>(accountDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable UUID accountId) {
        return accountService.getAccountById(accountId)
                .map(account -> new ResponseEntity<>(account, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountDTO> getAccountByNumber(@PathVariable String accountNumber) {
        return accountService.getAccountByNumber(accountNumber)
                .map(account -> new ResponseEntity<>(account, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountDTO>> getAccountsByUserId(@PathVariable UUID userId) {
        List<AccountDTO> accounts = accountService.getAccountsByUserId(userId);
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        List<AccountDTO> accounts = accountService.getAllAccounts();
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<AccountDTO> updateAccount(@PathVariable UUID accountId,
                                                    @Valid @RequestBody UpdateAccountRequest request) {
        try {
            AccountDTO updatedAccount = accountService.updateAccount(accountId, request);
            return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{accountId}/status")
    public ResponseEntity<AccountDTO> updateAccountStatus(@PathVariable UUID accountId,
                                                          @RequestParam AccountStatus status) {
        try {
            AccountDTO updatedAccount = accountService.updateAccountStatus(accountId, status);
            return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID accountId) {
        try {
            accountService.deleteAccount(accountId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable UUID accountId) {
        try {
            BigDecimal balance = accountService.getAccountBalance(accountId);
            return new ResponseEntity<>(balance, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}/total-balance")
    public ResponseEntity<BigDecimal> getTotalBalanceByUserId(@PathVariable UUID userId) {
        BigDecimal totalBalance = accountService.getTotalBalanceByUserId(userId);
        return new ResponseEntity<>(totalBalance, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/active-count")
    public ResponseEntity<Long> getActiveAccountsCount(@PathVariable UUID userId) {
        long count = accountService.getActiveAccountsCountByUserId(userId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("Account Service is running", HttpStatus.OK);
    }
}