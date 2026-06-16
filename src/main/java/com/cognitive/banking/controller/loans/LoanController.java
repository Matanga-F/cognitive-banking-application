package com.cognitive.banking.controller.loans;

import com.cognitive.banking.annotation.RequiresPermission;
import com.cognitive.banking.annotation.RequiresRole;
import com.cognitive.banking.domain.enums.LoanStatus;
import com.cognitive.banking.domain.enums.UserRole;
import com.cognitive.banking.dto.CreateLoanRequest;
import com.cognitive.banking.dto.LoanDTO;
import com.cognitive.banking.dto.LoanPaymentDTO;
import com.cognitive.banking.service.LoanService;
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
@RequestMapping("/loans")
@Tag(name = "Loan Management", description = "Endpoints for managing loans")
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);

    @Autowired
    private LoanService loanService;

    // ============================================
    // CREATE LOAN APPLICATION
    // ============================================
    @PostMapping("/apply")
    @RequiresPermission("loan:create")
    @Operation(summary = "Apply for a loan", description = "Submits a new loan application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan application submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<LoanDTO> applyForLoan(@Valid @RequestBody CreateLoanRequest request) {
        logger.info("REST request to apply for loan: {} for user: {}", request.getLoanType(), request.getUserId());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        // Verify user can only apply for loans for themselves unless admin
        if (!isAdmin() && !currentUserEmail.equals(getUserEmailFromId(request.getUserId()))) {
            logger.warn("User {} attempted to apply for loan for another user: {}", currentUserEmail, request.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LoanDTO result = loanService.createLoanApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ============================================
    // GET LOAN BY ID
    // ============================================
    @GetMapping("/{loanId}")
    @RequiresPermission("loan:read")
    @Operation(summary = "Get loan by ID", description = "Retrieves loan details by loan ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan found"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<LoanDTO> getLoanById(
            @Parameter(description = "Loan ID", required = true)
            @PathVariable UUID loanId) {
        logger.debug("REST request to get loan by ID: {}", loanId);

        if (!canAccessLoan(loanId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return loanService.getLoanById(loanId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================
    // GET LOANS BY USER ID
    // ============================================
    @GetMapping("/user/{userId}")
    @RequiresPermission("loan:read")
    @Operation(summary = "Get loans by user ID", description = "Retrieves all loans for a specific user")
    public ResponseEntity<List<LoanDTO>> getLoansByUserId(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId) {
        logger.info("REST request to get loans for user: {}", userId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        // Users can only view their own loans unless admin
        if (!isAdmin() && !currentUserEmail.equals(getUserEmailFromId(userId))) {
            logger.warn("User {} attempted to view loans of user: {}", currentUserEmail, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LoanDTO> loans = loanService.getLoansByUserId(userId);
        return ResponseEntity.ok(loans);
    }

    // ============================================
    // GET MY LOANS - Convenience for current user
    // ============================================
    @GetMapping("/my-loans")
    @RequiresPermission("loan:read")
    @Operation(summary = "Get my loans", description = "Retrieves all loans for the currently authenticated user")
    public ResponseEntity<List<LoanDTO>> getMyLoans() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        logger.info("REST request to get loans for current user: {}", email);

        UUID userId = getUserIdFromEmail(email);
        List<LoanDTO> loans = loanService.getLoansByUserId(userId);
        return ResponseEntity.ok(loans);
    }

    // ============================================
    // GET LOAN PAYMENTS
    // ============================================
    @GetMapping("/{loanId}/payments")
    @RequiresPermission("loan:read")
    @Operation(summary = "Get loan payments", description = "Retrieves all payment schedule for a loan")
    public ResponseEntity<List<LoanPaymentDTO>> getLoanPayments(
            @Parameter(description = "Loan ID", required = true)
            @PathVariable UUID loanId) {
        logger.debug("REST request to get payments for loan: {}", loanId);

        if (!canAccessLoan(loanId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LoanPaymentDTO> payments = loanService.getLoanPayments(loanId);
        return ResponseEntity.ok(payments);
    }

    // ============================================
    // APPROVE LOAN - Admin only
    // ============================================
    @PostMapping("/{loanId}/approve")
    @RequiresRole(UserRole.ADMIN)
    @RequiresPermission("loan:update")
    @Operation(summary = "Approve loan", description = "Approves a pending loan application (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan approved successfully"),
            @ApiResponse(responseCode = "400", description = "Loan cannot be approved"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<LoanDTO> approveLoan(
            @Parameter(description = "Loan ID", required = true)
            @PathVariable UUID loanId) {
        logger.info("REST request to approve loan: {}", loanId);

        LoanDTO result = loanService.approveLoan(loanId);
        return ResponseEntity.ok(result);
    }

    // ============================================
    // DISBURSE LOAN - Admin only
    // ============================================
    @PostMapping("/{loanId}/disburse")
    @RequiresRole(UserRole.ADMIN)
    @RequiresPermission("loan:update")
    @Operation(summary = "Disburse loan", description = "Disburses an approved loan to the account (Admin only)")
    public ResponseEntity<LoanDTO> disburseLoan(
            @Parameter(description = "Loan ID", required = true)
            @PathVariable UUID loanId) {
        logger.info("REST request to disburse loan: {}", loanId);

        LoanDTO result = loanService.disburseLoan(loanId);
        return ResponseEntity.ok(result);
    }

    // ============================================
    // REJECT LOAN - Admin only
    // ============================================
    @PostMapping("/{loanId}/reject")
    @RequiresRole(UserRole.ADMIN)
    @RequiresPermission("loan:update")
    @Operation(summary = "Reject loan", description = "Rejects a loan application (Admin only)")
    public ResponseEntity<Void> rejectLoan(
            @Parameter(description = "Loan ID", required = true)
            @PathVariable UUID loanId) {
        logger.info("REST request to reject loan: {}", loanId);

        loanService.rejectLoan(loanId);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // PROCESS LOAN PAYMENT
    // ============================================
    @PostMapping("/{loanId}/pay")
    @RequiresPermission("loan:update")
    @Operation(summary = "Process loan payment", description = "Processes a payment for a loan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment amount or insufficient funds"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<LoanPaymentDTO> processLoanPayment(
            @Parameter(description = "Loan ID", required = true)
            @PathVariable UUID loanId,
            @Parameter(description = "Payment amount", required = true)
            @RequestParam BigDecimal amount) {
        logger.info("REST request to process payment of {} for loan: {}", amount, loanId);

        if (!canAccessLoan(loanId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LoanPaymentDTO result = loanService.processLoanPayment(loanId, amount);
        return ResponseEntity.ok(result);
    }

    // ============================================
    // GET OUTSTANDING BALANCE
    // ============================================
    @GetMapping("/outstanding-balance")
    @RequiresPermission("loan:read")
    @Operation(summary = "Get my outstanding balance", description = "Retrieves total outstanding loan balance for current user")
    public ResponseEntity<BigDecimal> getOutstandingBalance() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        logger.info("REST request to get outstanding balance for user: {}", email);

        UUID userId = getUserIdFromEmail(email);
        BigDecimal balance = loanService.getOutstandingBalance(userId);
        return ResponseEntity.ok(balance);
    }

    // ============================================
    // GET ACTIVE LOANS COUNT
    // ============================================
    @GetMapping("/active-count")
    @RequiresPermission("loan:read")
    @Operation(summary = "Get my active loans count", description = "Retrieves count of active loans for current user")
    public ResponseEntity<Long> getActiveLoansCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        UUID userId = getUserIdFromEmail(email);
        long count = loanService.getActiveLoansCount(userId);
        return ResponseEntity.ok(count);
    }

    // ============================================
    // GET ALL LOANS - Admin only
    // ============================================
    @GetMapping("/all")
    @RequiresRole(UserRole.ADMIN)
    @RequiresPermission("loan:read")
    @Operation(summary = "Get all loans", description = "Retrieves all loans in the system (Admin only)")
    public ResponseEntity<List<LoanDTO>> getAllLoans() {
        logger.info("REST request to get all loans (Admin)");
        // Add this method to LoanService if needed
        List<LoanDTO> loans = loanService.getAllLoans();
        return ResponseEntity.ok(loans);
    }

    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean canAccessLoan(UUID loanId) {
        if (isAdmin()) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        UUID currentUserId = getUserIdFromEmail(currentUserEmail);

        return loanService.getLoanById(loanId)
                .map(loan -> loan.getUserId() != null && loan.getUserId().equals(currentUserId))
                .orElse(false);
    }

    private UUID getUserIdFromEmail(String email) {
        // This should call your UserService
        // For now, return null - implement properly
        return null;
    }

    private String getUserEmailFromId(UUID userId) {
        // This should call your UserService
        // For now, return null - implement properly
        return null;
    }
}