package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.TransactionType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateTransactionRequest {
    private String accountNumber;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private String recipientAccountNumber;
    private String recipientName;
}