// src/main/java/com/cognitive/banking/service/LoanService.java
package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.Account;
import com.cognitive.banking.domain.entity.Loan;
import com.cognitive.banking.domain.entity.LoanPayment;
import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.LoanStatus;
import com.cognitive.banking.domain.enums.LoanType;
import com.cognitive.banking.dto.CreateLoanRequest;
import com.cognitive.banking.dto.LoanDTO;
import com.cognitive.banking.dto.LoanPaymentDTO;
import com.cognitive.banking.repository.AccountRepository;
import com.cognitive.banking.repository.LoanPaymentRepository;
import com.cognitive.banking.repository.LoanRepository;
import com.cognitive.banking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanPaymentRepository loanPaymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    public LoanDTO createLoanApplication(CreateLoanRequest request) {
        System.out.println("Creating new loan application for user ID: " + request.getUserId());

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        // Validate account exists and belongs to user
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + request.getAccountId()));

        if (!account.getUser().getUserId().equals(request.getUserId())) {
            throw new RuntimeException("Account does not belong to the specified user");
        }

        // Generate unique loan number
        String loanNumber = generateLoanNumber();

        // Calculate monthly payment
        BigDecimal monthlyPayment = calculateMonthlyPayment(
                request.getPrincipalAmount(),
                request.getInterestRate(),
                request.getTermMonths()
        );

        // Create loan entity with ALL required fields
        Loan loan = new Loan();
        loan.setLoanNumber(loanNumber);
        loan.setLoanType(request.getLoanType());
        loan.setLoanStatus(LoanStatus.PENDING);  // REQUIRED: NOT NULL
        loan.setPrincipalAmount(request.getPrincipalAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setTermMonths(request.getTermMonths());
        loan.setRemainingTermMonths(request.getTermMonths());  // REQUIRED: NOT NULL
        loan.setMonthlyPayment(monthlyPayment);
        loan.setRemainingBalance(request.getPrincipalAmount());  // REQUIRED: NOT NULL
        loan.setMaturityDate(LocalDate.now().plusMonths(request.getTermMonths()));  // REQUIRED: NOT NULL
        loan.setUser(user);
        loan.setAccount(account);
        loan.setPurpose(request.getPurpose());
        loan.setCollateralDescription(request.getCollateralDescription());

        // Initialize other fields
        loan.setTotalInterestPaid(BigDecimal.ZERO);
        loan.setTotalAmountPaid(BigDecimal.ZERO);

        Loan savedLoan = loanRepository.save(loan);
        System.out.println("Loan application created successfully with number: " + savedLoan.getLoanNumber());

        return convertToDTO(savedLoan);
    }

    public LoanDTO approveLoan(UUID loanId) {
        System.out.println("Approving loan with ID: " + loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        if (loan.getLoanStatus() != LoanStatus.PENDING) {
            throw new RuntimeException("Loan is not in PENDING status");
        }

        loan.setLoanStatus(LoanStatus.APPROVED);
        Loan updatedLoan = loanRepository.save(loan);

        System.out.println("Loan approved successfully");
        return convertToDTO(updatedLoan);
    }

    public LoanDTO disburseLoan(UUID loanId) {
        System.out.println("Disbursing loan with ID: " + loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        if (loan.getLoanStatus() != LoanStatus.APPROVED) {
            throw new RuntimeException("Loan is not in APPROVED status");
        }

        // Disburse funds to account
        Account account = loan.getAccount();
        account.setBalance(account.getBalance().add(loan.getPrincipalAmount()));
        accountRepository.save(account);

        // Update loan status and dates
        loan.setLoanStatus(LoanStatus.ACTIVE);
        loan.setDisbursementDate(LocalDate.now());
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));

        // Create initial payment schedule
        createPaymentSchedule(loan);

        Loan updatedLoan = loanRepository.save(loan);
        System.out.println("Loan disbursed successfully");

        return convertToDTO(updatedLoan);
    }

    public LoanPaymentDTO processLoanPayment(UUID loanId, BigDecimal paymentAmount) {
        System.out.println("Processing payment for loan ID: " + loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        if (loan.getLoanStatus() != LoanStatus.ACTIVE && loan.getLoanStatus() != LoanStatus.DELINQUENT) {
            throw new RuntimeException("Loan is not active");
        }

        // Get the next due payment
        List<LoanPayment> pendingPayments = loanPaymentRepository.findPendingPaymentsByLoanId(loanId);

        if (pendingPayments.isEmpty()) {
            throw new RuntimeException("No pending payments found for this loan");
        }

        LoanPayment nextPayment = pendingPayments.get(0);

        // Check if payment covers at least the minimum due
        if (paymentAmount.compareTo(nextPayment.getTotalPayment()) < 0) {
            throw new RuntimeException("Payment amount is less than the minimum due: " + nextPayment.getTotalPayment());
        }

        // Process payment
        Account account = loan.getAccount();
        if (account.getBalance().compareTo(paymentAmount) < 0) {
            throw new RuntimeException("Insufficient funds in account");
        }

        // Deduct payment from account
        account.setBalance(account.getBalance().subtract(paymentAmount));
        accountRepository.save(account);

        // Update payment record
        nextPayment.markAsPaid();

        // Update loan balance and statistics
        BigDecimal principalPaid = nextPayment.getPrincipalAmount();
        BigDecimal interestPaid = nextPayment.getInterestAmount();

        loan.setRemainingBalance(loan.getRemainingBalance().subtract(principalPaid));
        loan.setRemainingTermMonths(loan.getRemainingTermMonths() - 1);
        loan.setTotalInterestPaid(loan.getTotalInterestPaid().add(interestPaid));
        loan.setTotalAmountPaid(loan.getTotalAmountPaid().add(paymentAmount));

        // Update next payment date
        if (loan.getRemainingTermMonths() > 0) {
            loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        } else {
            // Loan is paid off
            loan.setLoanStatus(LoanStatus.PAID_OFF);
            loan.setNextPaymentDate(null);
        }

        // Check for overpayment
        BigDecimal overpayment = paymentAmount.subtract(nextPayment.getTotalPayment());
        if (overpayment.compareTo(BigDecimal.ZERO) > 0) {
            // Apply overpayment to principal
            loan.setRemainingBalance(loan.getRemainingBalance().subtract(overpayment));
            loan.setTotalAmountPaid(loan.getTotalAmountPaid().add(overpayment));
        }

        loanRepository.save(loan);
        LoanPayment savedPayment = loanPaymentRepository.save(nextPayment);

        System.out.println("Loan payment processed successfully");
        return convertToPaymentDTO(savedPayment);
    }

    @Transactional(readOnly = true)
    public Optional<LoanDTO> getLoanById(UUID loanId) {
        System.out.println("Fetching loan by ID: " + loanId);
        return loanRepository.findById(loanId)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<LoanDTO> getLoanByNumber(String loanNumber) {
        System.out.println("Fetching loan by number: " + loanNumber);
        return loanRepository.findByLoanNumber(loanNumber)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<LoanDTO> getLoansByUserId(UUID userId) {
        System.out.println("Fetching loans for user ID: " + userId);
        return loanRepository.findByUserUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanDTO> getAllLoans() {
        System.out.println("Fetching all loans");
        return loanRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanPaymentDTO> getLoanPayments(UUID loanId) {
        System.out.println("Fetching payments for loan ID: " + loanId);
        return loanPaymentRepository.findByLoanLoanId(loanId).stream()
                .map(this::convertToPaymentDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getOutstandingBalance(UUID userId) {
        BigDecimal totalBalance = loanRepository.getTotalOutstandingBalanceByUserId(userId);
        return totalBalance != null ? totalBalance : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long getActiveLoansCount(UUID userId) {
        return loanRepository.countActiveLoansByUserId(userId);
    }

    public LoanDTO updateLoanStatus(UUID loanId, LoanStatus status) {
        System.out.println("Updating loan status to " + status + " for loan ID: " + loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        loan.setLoanStatus(status);
        Loan updatedLoan = loanRepository.save(loan);

        System.out.println("Loan status updated successfully");
        return convertToDTO(updatedLoan);
    }

    public void rejectLoan(UUID loanId) {
        System.out.println("Rejecting loan with ID: " + loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        loan.setLoanStatus(LoanStatus.REJECTED);
        loanRepository.save(loan);

        System.out.println("Loan rejected successfully");
    }

    private void createPaymentSchedule(Loan loan) {
        BigDecimal monthlyInterestRate = loan.getInterestRate().divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP);
        BigDecimal remainingBalance = loan.getPrincipalAmount();

        for (int i = 1; i <= loan.getTermMonths(); i++) {
            BigDecimal interestAmount = remainingBalance.multiply(monthlyInterestRate)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalAmount = loan.getMonthlyPayment().subtract(interestAmount);

            // Adjust last payment
            if (i == loan.getTermMonths()) {
                principalAmount = remainingBalance;
            }

            remainingBalance = remainingBalance.subtract(principalAmount);

            LocalDate dueDate = loan.getDisbursementDate().plusMonths(i);

            LoanPayment payment = new LoanPayment(
                    loan,
                    i,
                    dueDate,
                    principalAmount,
                    interestAmount,
                    remainingBalance.max(BigDecimal.ZERO)
            );

            loanPaymentRepository.save(payment);
        }
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRate, Integer termMonths) {
        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP);
        BigDecimal rateFactor = BigDecimal.ONE.add(monthlyRate);
        BigDecimal denominator = BigDecimal.ONE.subtract(rateFactor.pow(-termMonths, MathContext.DECIMAL64));

        return principal.multiply(monthlyRate).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private String generateLoanNumber() {
        String loanNumber;
        do {
            // Generate 10-digit loan number
            long number = (long) (Math.random() * 1_000_000_000L);
            loanNumber = "LN" + String.format("%010d", number);
        } while (loanRepository.existsByLoanNumber(loanNumber));

        return loanNumber;
    }

    private LoanDTO convertToDTO(Loan loan) {
        String userName = loan.getUser().getFirstName() + " " + loan.getUser().getLastName();
        String accountNumber = loan.getAccount() != null ? loan.getAccount().getAccountNumber() : null;
        UUID accountId = loan.getAccount() != null ? loan.getAccount().getAccountId() : null;

        return new LoanDTO(
                loan.getLoanId(),
                loan.getLoanNumber(),
                loan.getLoanType(),
                loan.getLoanStatus(),
                loan.getPrincipalAmount(),
                loan.getInterestRate(),
                loan.getTermMonths(),
                loan.getRemainingTermMonths(),
                loan.getMonthlyPayment(),
                loan.getRemainingBalance(),
                loan.getTotalInterestPaid(),
                loan.getTotalAmountPaid(),
                loan.getNextPaymentDate(),
                loan.getMaturityDate(),
                loan.getDisbursementDate(),
                loan.getUser().getUserId(),
                userName,
                accountId,
                accountNumber,
                loan.getPurpose(),
                loan.getCollateralDescription(),
                loan.getCreatedAt(),
                loan.getUpdatedAt()
        );
    }

    private LoanPaymentDTO convertToPaymentDTO(LoanPayment payment) {
        return new LoanPaymentDTO(
                payment.getPaymentId(),
                payment.getLoan().getLoanId(),
                payment.getLoan().getLoanNumber(),
                payment.getPaymentNumber(),
                payment.getPaymentDate(),
                payment.getDueDate(),
                payment.getPrincipalAmount(),
                payment.getInterestAmount(),
                payment.getTotalPayment(),
                payment.getRemainingBalance(),
                payment.getPaymentStatus(),
                payment.getPaidDate(),
                payment.getLateFee(),
                payment.getCreatedAt()
        );
    }
}