// src/main/java/com/cognitive/banking/dto/CreateAccountRequest.java
package com.cognitive.banking.dto;

import com.cognitive.banking.domain.enums.AccountType;
import com.cognitive.banking.domain.enums.Currency;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    private Long userId;
    private AccountType accountType;
    private Currency currency;
    private BigDecimal initialDeposit;

    // Explicit getters/setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public BigDecimal getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; }
}