// src/main/java/com/cognitive/banking/domain/enums/TransactionType.java
package com.cognitive.banking.domain.enums;

public enum TransactionType {
    // Debit transactions
    PURCHASE,
    ATM_WITHDRAWAL,
    TRANSFER_OUT,
    FEE,
    PAYMENT,

    // Credit transactions
    DEPOSIT,
    TRANSFER_IN,
    REFUND,
    INTEREST,
    CASHBACK,

    // System transactions
    ADJUSTMENT,
    REVERSAL
}