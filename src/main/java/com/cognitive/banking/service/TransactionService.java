// src/main/java/com/cognitive/banking/service/TransactionService.java
package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.Transaction;
import com.cognitive.banking.dto.TransactionDTO;
import com.cognitive.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionDTO> getAccountTransactions(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findByAccountAccountNumberOrderByCreatedAtDesc(accountNumber);
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getUserTransactions(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setTransactionReference(transaction.getTransactionReference());
        dto.setAccountNumber(transaction.getAccount().getAccountNumber());
        dto.setType(transaction.getType());
        dto.setStatus(transaction.getStatus());
        dto.setAmount(transaction.getAmount());
        dto.setBalanceAfter(transaction.getBalanceAfter());
        dto.setRecipientAccountNumber(transaction.getRecipientAccountNumber());
        dto.setRecipientName(transaction.getRecipientName());
        dto.setDescription(transaction.getDescription());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setProcessedAt(transaction.getProcessedAt());
        return dto;
    }
}