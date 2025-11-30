// src/main/java/com/cognitive/banking/controller/LoanController.java
package com.cognitive.banking.controller;

import com.cognitive.banking.domain.enums.LoanStatus;
import com.cognitive.banking.dto.CreateLoanRequest;
import com.cognitive.banking.dto.LoanDTO;
import com.cognitive.banking.dto.LoanPaymentDTO;
import com.cognitive.banking.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanDTO> createLoanApplication(@Valid @RequestBody CreateLoanRequest request) {
        try {
            LoanDTO loanDTO = loanService.createLoanApplication(request);
            return new ResponseEntity<>(loanDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{loanId}/approve")
    public ResponseEntity<LoanDTO> approveLoan(@PathVariable UUID loanId) {
        try {
            LoanDTO loanDTO = loanService.approveLoan(loanId);
            return new ResponseEntity<>(loanDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<LoanDTO> disburseLoan(@PathVariable UUID loanId) {
        try {
            LoanDTO loanDTO = loanService.disburseLoan(loanId);
            return new ResponseEntity<>(loanDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{loanId}/payments")
    public ResponseEntity<LoanPaymentDTO> processLoanPayment(
            @PathVariable UUID loanId,
            @RequestParam BigDecimal amount) {
        try {
            LoanPaymentDTO paymentDTO = loanService.processLoanPayment(loanId, amount);
            return new ResponseEntity<>(paymentDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanDTO> getLoanById(@PathVariable UUID loanId) {
        return loanService.getLoanById(loanId)
                .map(loan -> new ResponseEntity<>(loan, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/number/{loanNumber}")
    public ResponseEntity<LoanDTO> getLoanByNumber(@PathVariable String loanNumber) {
        return loanService.getLoanByNumber(loanNumber)
                .map(loan -> new ResponseEntity<>(loan, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanDTO>> getLoansByUserId(@PathVariable UUID userId) {
        List<LoanDTO> loans = loanService.getLoansByUserId(userId);
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<LoanDTO>> getAllLoans() {
        List<LoanDTO> loans = loanService.getAllLoans();
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    @GetMapping("/{loanId}/payments")
    public ResponseEntity<List<LoanPaymentDTO>> getLoanPayments(@PathVariable UUID loanId) {
        List<LoanPaymentDTO> payments = loanService.getLoanPayments(loanId);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/outstanding-balance")
    public ResponseEntity<BigDecimal> getOutstandingBalance(@PathVariable UUID userId) {
        BigDecimal balance = loanService.getOutstandingBalance(userId);
        return new ResponseEntity<>(balance, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/active-count")
    public ResponseEntity<Long> getActiveLoansCount(@PathVariable UUID userId) {
        long count = loanService.getActiveLoansCount(userId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @PatchMapping("/{loanId}/status")
    public ResponseEntity<LoanDTO> updateLoanStatus(@PathVariable UUID loanId,
                                                    @RequestParam LoanStatus status) {
        try {
            LoanDTO updatedLoan = loanService.updateLoanStatus(loanId, status);
            return new ResponseEntity<>(updatedLoan, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{loanId}/reject")
    public ResponseEntity<Void> rejectLoan(@PathVariable UUID loanId) {
        try {
            loanService.rejectLoan(loanId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("Loan Service is running", HttpStatus.OK);
    }
}