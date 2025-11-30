// src/main/java/com/cognitive/banking/controller/TransactionController.java
package com.cognitive.banking.controller;

import com.cognitive.banking.domain.enums.TransactionStatus;
import com.cognitive.banking.dto.CreateTransactionRequest;
import com.cognitive.banking.dto.TransactionDTO;
import com.cognitive.banking.dto.TransferRequest;
import com.cognitive.banking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        try {
            TransactionDTO transactionDTO = transactionService.createTransaction(request);
            return new ResponseEntity<>(transactionDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDTO> processTransfer(@Valid @RequestBody TransferRequest request) {
        try {
            TransactionDTO transactionDTO = transactionService.processTransfer(request);
            return new ResponseEntity<>(transactionDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable UUID transactionId) {
        return transactionService.getTransactionById(transactionId)
                .map(transaction -> new ResponseEntity<>(transaction, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/reference/{transactionReference}")
    public ResponseEntity<TransactionDTO> getTransactionByReference(@PathVariable String transactionReference) {
        return transactionService.getTransactionByReference(transactionReference)
                .map(transaction -> new ResponseEntity<>(transaction, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccountId(@PathVariable UUID accountId) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByAccountId(accountId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByCardId(@PathVariable UUID cardId) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByCardId(cardId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/account/{accountId}/date-range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @PathVariable UUID accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByDateRange(accountId, startDate, endDate);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<TransactionDTO> transactions = transactionService.getAllTransactions();
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @PatchMapping("/{transactionId}/status")
    public ResponseEntity<TransactionDTO> updateTransactionStatus(@PathVariable UUID transactionId,
                                                                  @RequestParam TransactionStatus status) {
        try {
            TransactionDTO updatedTransaction = transactionService.updateTransactionStatus(transactionId, status);
            return new ResponseEntity<>(updatedTransaction, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/account/{accountId}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable UUID accountId) {
        try {
            BigDecimal balance = transactionService.getAccountBalance(accountId);
            return new ResponseEntity<>(balance, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/account/{accountId}/summary")
    public ResponseEntity<String> getTransactionSummary(
            @PathVariable UUID accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            BigDecimal totalDebits = transactionService.getTotalDebits(accountId, startDate, endDate);
            BigDecimal totalCredits = transactionService.getTotalCredits(accountId, startDate, endDate);
            BigDecimal netFlow = totalCredits.subtract(totalDebits);

            String summary = String.format(
                    "Transaction Summary for Account %s:\nTotal Debits: $%.2f\nTotal Credits: $%.2f\nNet Flow: $%.2f",
                    accountId, totalDebits, totalCredits, netFlow
            );

            return new ResponseEntity<>(summary, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("Transaction Service is running", HttpStatus.OK);
    }
}