package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.Account;
import com.cognitive.banking.domain.entity.Loan;
import com.cognitive.banking.domain.entity.LoanPayment;
import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.LoanStatus;
import com.cognitive.banking.dto.CreateLoanRequest;
import com.cognitive.banking.dto.LoanDTO;
import com.cognitive.banking.dto.LoanPaymentDTO;
import com.cognitive.banking.repository.AccountRepository;
import com.cognitive.banking.repository.LoanPaymentRepository;
import com.cognitive.banking.repository.LoanRepository;
import com.cognitive.banking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PhoneService phoneService;

    public LoanService(LoanRepository loanRepository,
                       LoanPaymentRepository loanPaymentRepository,
                       UserRepository userRepository,
                       AccountRepository accountRepository,
                       EmailService emailService,
                       PhoneService phoneService) {
        this.loanRepository = loanRepository;
        this.loanPaymentRepository = loanPaymentRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.emailService = emailService;
        this.phoneService = phoneService;
    }

    public LoanDTO createLoanApplication(CreateLoanRequest request) {
        logger.info("Creating new loan application for user ID: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getUserId().equals(request.getUserId())) {
            throw new RuntimeException("Account does not belong to the specified user");
        }

        String loanNumber = generateLoanNumber();
        BigDecimal monthlyPayment = calculateMonthlyPayment(
                request.getPrincipalAmount(),
                request.getInterestRate(),
                request.getTermMonths()
        );

        Loan loan = new Loan();
        loan.setLoanNumber(loanNumber);
        loan.setLoanType(request.getLoanType());
        loan.setLoanStatus(LoanStatus.PENDING);
        loan.setPrincipalAmount(request.getPrincipalAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setTermMonths(request.getTermMonths());
        loan.setRemainingTermMonths(request.getTermMonths());
        loan.setMonthlyPayment(monthlyPayment);
        loan.setRemainingBalance(request.getPrincipalAmount());
        loan.setMaturityDate(LocalDate.now().plusMonths(request.getTermMonths()));
        loan.setUser(user);
        loan.setAccount(account);
        loan.setPurpose(request.getPurpose());
        loan.setCollateralDescription(request.getCollateralDescription());
        loan.setTotalInterestPaid(BigDecimal.ZERO);
        loan.setTotalAmountPaid(BigDecimal.ZERO);
        loan.setCreatedAt(LocalDateTime.now());

        Loan savedLoan = loanRepository.save(loan);
        logger.info("Loan application created successfully with number: {}", savedLoan.getLoanNumber());

        // Send notification
        sendLoanApplicationNotification(user, savedLoan);

        return convertToDTO(savedLoan);
    }

    public LoanDTO approveLoan(UUID loanId) {
        logger.info("Approving loan with ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getLoanStatus() != LoanStatus.PENDING) {
            throw new RuntimeException("Loan is not in PENDING status");
        }

        loan.setLoanStatus(LoanStatus.APPROVED);
        loan.setUpdatedAt(LocalDateTime.now());
        Loan updatedLoan = loanRepository.save(loan);

        // Send approval notification
        sendLoanApprovalNotification(loan.getUser(), updatedLoan);

        logger.info("Loan approved successfully: {}", loan.getLoanNumber());
        return convertToDTO(updatedLoan);
    }

    public LoanDTO disburseLoan(UUID loanId) {
        logger.info("Disbursing loan with ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getLoanStatus() != LoanStatus.APPROVED) {
            throw new RuntimeException("Loan is not in APPROVED status");
        }

        Account account = loan.getAccount();
        account.setBalance(account.getBalance().add(loan.getPrincipalAmount()));
        accountRepository.save(account);

        loan.setLoanStatus(LoanStatus.ACTIVE);
        loan.setDisbursementDate(LocalDate.now());
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        loan.setUpdatedAt(LocalDateTime.now());

        createPaymentSchedule(loan);

        Loan updatedLoan = loanRepository.save(loan);

        // Send disbursement notification
        sendLoanDisbursementNotification(loan.getUser(), updatedLoan);

        logger.info("Loan disbursed successfully: {}", loan.getLoanNumber());
        return convertToDTO(updatedLoan);
    }

    public LoanPaymentDTO processLoanPayment(UUID loanId, BigDecimal paymentAmount) {
        logger.info("Processing payment for loan ID: {} of amount {}", loanId, paymentAmount);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getLoanStatus() != LoanStatus.ACTIVE && loan.getLoanStatus() != LoanStatus.DELINQUENT) {
            throw new RuntimeException("Loan is not active");
        }

        List<LoanPayment> pendingPayments = loanPaymentRepository.findPendingPaymentsByLoanId(loanId);
        if (pendingPayments.isEmpty()) {
            throw new RuntimeException("No pending payments found for this loan");
        }

        LoanPayment nextPayment = pendingPayments.get(0);

        if (paymentAmount.compareTo(nextPayment.getTotalPayment()) < 0) {
            throw new RuntimeException("Payment amount is less than the minimum due: " + nextPayment.getTotalPayment());
        }

        Account account = loan.getAccount();
        if (account.getBalance().compareTo(paymentAmount) < 0) {
            throw new RuntimeException("Insufficient funds in account");
        }

        account.setBalance(account.getBalance().subtract(paymentAmount));
        accountRepository.save(account);

        nextPayment.markAsPaid();
        nextPayment.setPaidDate(LocalDate.now());

        BigDecimal principalPaid = nextPayment.getPrincipalAmount();
        BigDecimal interestPaid = nextPayment.getInterestAmount();

        loan.setRemainingBalance(loan.getRemainingBalance().subtract(principalPaid));
        loan.setRemainingTermMonths(loan.getRemainingTermMonths() - 1);
        loan.setTotalInterestPaid(loan.getTotalInterestPaid().add(interestPaid));
        loan.setTotalAmountPaid(loan.getTotalAmountPaid().add(paymentAmount));

        if (loan.getRemainingTermMonths() > 0) {
            loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        } else {
            loan.setLoanStatus(LoanStatus.PAID_OFF);
            loan.setNextPaymentDate(null);
        }

        BigDecimal overpayment = paymentAmount.subtract(nextPayment.getTotalPayment());
        if (overpayment.compareTo(BigDecimal.ZERO) > 0) {
            loan.setRemainingBalance(loan.getRemainingBalance().subtract(overpayment));
            loan.setTotalAmountPaid(loan.getTotalAmountPaid().add(overpayment));
        }

        loan.setUpdatedAt(LocalDateTime.now());
        loanRepository.save(loan);
        LoanPayment savedPayment = loanPaymentRepository.save(nextPayment);

        // Send payment confirmation
        sendLoanPaymentNotification(loan.getUser(), loan, savedPayment);

        logger.info("Loan payment processed successfully for loan: {}", loan.getLoanNumber());
        return convertToPaymentDTO(savedPayment);
    }

    @Transactional(readOnly = true)
    public Optional<LoanDTO> getLoanById(UUID loanId) {
        logger.debug("Fetching loan by ID: {}", loanId);
        return loanRepository.findById(loanId).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<LoanDTO> getLoansByUserId(UUID userId) {
        logger.debug("Fetching loans for user ID: {}", userId);
        return loanRepository.findByUserUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanPaymentDTO> getLoanPayments(UUID loanId) {
        logger.debug("Fetching payments for loan ID: {}", loanId);
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
    public List<LoanDTO> getAllLoans() {
        logger.info("Fetching all loans");
        return loanRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getActiveLoansCount(UUID userId) {
        return loanRepository.countActiveLoansByUserId(userId);
    }

    public void rejectLoan(UUID loanId) {
        logger.info("Rejecting loan with ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setLoanStatus(LoanStatus.REJECTED);
        loan.setUpdatedAt(LocalDateTime.now());
        loanRepository.save(loan);

        sendLoanRejectionNotification(loan.getUser(), loan);
        logger.info("Loan rejected: {}", loan.getLoanNumber());
    }

    private void createPaymentSchedule(Loan loan) {
        BigDecimal monthlyInterestRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP);
        BigDecimal remainingBalance = loan.getPrincipalAmount();

        for (int i = 1; i <= loan.getTermMonths(); i++) {
            BigDecimal interestAmount = remainingBalance.multiply(monthlyInterestRate)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalAmount = loan.getMonthlyPayment().subtract(interestAmount);

            if (i == loan.getTermMonths()) {
                principalAmount = remainingBalance;
            }

            remainingBalance = remainingBalance.subtract(principalAmount);
            LocalDate dueDate = loan.getDisbursementDate().plusMonths(i);

            LoanPayment payment = new LoanPayment();
            payment.setLoan(loan);
            payment.setPaymentNumber(i);
            payment.setDueDate(dueDate);
            payment.setPrincipalAmount(principalAmount);
            payment.setInterestAmount(interestAmount);
            payment.setTotalPayment(principalAmount.add(interestAmount));
            payment.setRemainingBalance(remainingBalance.max(BigDecimal.ZERO));
            payment.setPaymentStatus(LoanPayment.PaymentStatus.PENDING);
            payment.setCreatedAt(LocalDateTime.now());

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
            long number = (long) (Math.random() * 1_000_000_000L);
            loanNumber = "LN" + String.format("%010d", number);
        } while (loanRepository.existsByLoanNumber(loanNumber));
        return loanNumber;
    }

    private void sendLoanApplicationNotification(User user, Loan loan) {
        try {
            emailService.sendEmail(user.getEmail(),
                    "Loan Application Received - Cognitive Banking",
                    String.format("Dear %s,\n\nYour loan application (Ref: %s) for %s has been received and is under review.\n\nYou will be notified once a decision has been made.\n\nBest regards,\nCognitive Banking Team",
                            user.getFirstName(), loan.getLoanNumber(), loan.getPrincipalAmount()));
        } catch (Exception e) {
            logger.warn("Failed to send loan application email: {}", e.getMessage());
        }
    }

    private void sendLoanApprovalNotification(User user, Loan loan) {
        try {
            emailService.sendEmail(user.getEmail(),
                    "Loan Approved - Cognitive Banking",
                    String.format("Dear %s,\n\nCongratulations! Your loan application (Ref: %s) has been APPROVED.\n\nLoan Amount: %s\nInterest Rate: %s%%\nMonthly Payment: %s\n\nPlease contact us to proceed with disbursement.\n\nBest regards,\nCognitive Banking Team",
                            user.getFirstName(), loan.getLoanNumber(), loan.getPrincipalAmount(), loan.getInterestRate(), loan.getMonthlyPayment()));
        } catch (Exception e) {
            logger.warn("Failed to send loan approval email: {}", e.getMessage());
        }
    }

    private void sendLoanDisbursementNotification(User user, Loan loan) {
        try {
            emailService.sendEmail(user.getEmail(),
                    "Loan Disbursed - Cognitive Banking",
                    String.format("Dear %s,\n\nYour loan (Ref: %s) has been DISBURSED.\n\nAmount: %s has been credited to your account.\n\nYour first payment is due on: %s\n\nBest regards,\nCognitive Banking Team",
                            user.getFirstName(), loan.getLoanNumber(), loan.getPrincipalAmount(), loan.getNextPaymentDate()));
        } catch (Exception e) {
            logger.warn("Failed to send loan disbursement email: {}", e.getMessage());
        }
    }

    private void sendLoanPaymentNotification(User user, Loan loan, LoanPayment payment) {
        try {
            emailService.sendEmail(user.getEmail(),
                    "Loan Payment Received - Cognitive Banking",
                    String.format("Dear %s,\n\nWe have received your loan payment for loan (Ref: %s).\n\nPayment Amount: %s\nRemaining Balance: %s\n\nThank you for your payment.\n\nBest regards,\nCognitive Banking Team",
                            user.getFirstName(), loan.getLoanNumber(), payment.getTotalPayment(), loan.getRemainingBalance()));
        } catch (Exception e) {
            logger.warn("Failed to send loan payment email: {}", e.getMessage());
        }
    }

    private void sendLoanRejectionNotification(User user, Loan loan) {
        try {
            emailService.sendEmail(user.getEmail(),
                    "Loan Application Status Update - Cognitive Banking",
                    String.format("Dear %s,\n\nThank you for your loan application (Ref: %s).\n\nAfter careful review, we regret to inform you that your application was not approved at this time.\n\nIf you have any questions, please contact our support team.\n\nBest regards,\nCognitive Banking Team",
                            user.getFirstName(), loan.getLoanNumber()));
        } catch (Exception e) {
            logger.warn("Failed to send loan rejection email: {}", e.getMessage());
        }
    }

    private LoanDTO convertToDTO(Loan loan) {
        LoanDTO dto = new LoanDTO();
        dto.setLoanId(loan.getLoanId());
        dto.setLoanNumber(loan.getLoanNumber());
        dto.setLoanType(loan.getLoanType());
        dto.setLoanStatus(loan.getLoanStatus());
        dto.setPrincipalAmount(loan.getPrincipalAmount());
        dto.setInterestRate(loan.getInterestRate());
        dto.setTermMonths(loan.getTermMonths());
        dto.setRemainingTermMonths(loan.getRemainingTermMonths());
        dto.setMonthlyPayment(loan.getMonthlyPayment());
        dto.setRemainingBalance(loan.getRemainingBalance());
        dto.setTotalInterestPaid(loan.getTotalInterestPaid());
        dto.setTotalAmountPaid(loan.getTotalAmountPaid());
        dto.setNextPaymentDate(loan.getNextPaymentDate());
        dto.setMaturityDate(loan.getMaturityDate());
        dto.setDisbursementDate(loan.getDisbursementDate());
        dto.setUserId(loan.getUser().getUserId());
        dto.setUserName(loan.getUser().getFirstName() + " " + loan.getUser().getLastName());
        dto.setAccountId(loan.getAccount().getAccountId());
        dto.setAccountNumber(maskAccountNumber(loan.getAccount().getAccountNumber()));
        dto.setPurpose(loan.getPurpose());
        dto.setCollateralDescription(loan.getCollateralDescription());
        dto.setCreatedAt(loan.getCreatedAt());
        dto.setUpdatedAt(loan.getUpdatedAt());
        return dto;
    }

    private LoanPaymentDTO convertToPaymentDTO(LoanPayment payment) {
        LoanPaymentDTO dto = new LoanPaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setLoanId(payment.getLoan().getLoanId());
        dto.setLoanNumber(payment.getLoan().getLoanNumber());
        dto.setPaymentNumber(payment.getPaymentNumber());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setDueDate(payment.getDueDate());
        dto.setPrincipalAmount(payment.getPrincipalAmount());
        dto.setInterestAmount(payment.getInterestAmount());
        dto.setTotalPayment(payment.getTotalPayment());
        dto.setRemainingBalance(payment.getRemainingBalance());
        dto.setPaymentStatus(payment.getPaymentStatus()); // expects enum or String? Use enum.
        dto.setPaidDate(payment.getPaidDate());
        dto.setLateFee(payment.getLateFee());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) return accountNumber;
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}