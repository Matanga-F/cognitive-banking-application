package com.cognitive.banking.monitoring.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class BankingMetrics {

    @Autowired
    private MeterRegistry meterRegistry;

    // ============================================
    // USER METRICS
    // ============================================
    private Counter userRegistrationsSuccess;
    private Counter userRegistrationsFailure;
    private Counter userLoginsSuccess;
    private Counter userLoginsFailure;
    private Counter userLogouts;
    private Counter passwordChangesSuccess;
    private Counter passwordChangesFailure;
    private Counter passwordResetInitiated;
    private Counter passwordResetSuccess;
    private Counter passwordResetFailure;

    // User Gauges
    private AtomicLong totalUsersGauge = new AtomicLong(0);
    private AtomicLong activeUsersGauge = new AtomicLong(0);
    private AtomicLong adminUsersGauge = new AtomicLong(0);
    private AtomicLong managerUsersGauge = new AtomicLong(0);
    private AtomicLong customerUsersGauge = new AtomicLong(0);
    private AtomicLong twoFactorEnabledGauge = new AtomicLong(0);
    private AtomicLong emailVerifiedGauge = new AtomicLong(0);
    private AtomicLong phoneVerifiedGauge = new AtomicLong(0);
    private AtomicLong lockedUsersGauge = new AtomicLong(0);

    // User Timers
    private Timer registrationTimer;
    private Timer loginTimer;
    private Timer passwordResetTimer;
    private Timer emailVerificationTimer;

    // ============================================
    // ACCOUNT METRICS
    // ============================================
    private Counter accountsCreated;
    private Counter accountsClosed;
    private Counter accountCreationFailures;
    private Counter accountUpdates;
    private Counter balanceChecks;
    private DistributionSummary accountBalances;
    private AtomicLong totalAccountsGauge = new AtomicLong(0);
    private AtomicLong activeAccountsGauge = new AtomicLong(0);
    private AtomicLong totalBalanceGauge = new AtomicLong(0);
    private Timer accountCreationTimer;
    private Timer accountQueryTimer;

    // ============================================
    // CARD METRICS
    // ============================================
    private Counter cardsIssued;
    private Counter cardsActivated;
    private Counter cardsBlocked;
    private Counter cardsCancelled;
    private Counter cardsLostStolen;
    private Counter cardsExpired;
    private Counter cardCreationFailures;
    private Counter cardActivationFailures;
    private Counter pinChangesSuccess;
    private Counter pinChangesFailure;
    private DistributionSummary cardLimits;
    private AtomicLong totalCardsGauge = new AtomicLong(0);
    private AtomicLong activeCardsGauge = new AtomicLong(0);
    private AtomicLong creditCardsGauge = new AtomicLong(0);
    private AtomicLong debitCardsGauge = new AtomicLong(0);
    private Timer cardCreationTimer;
    private Timer cardActivationTimer;
    private Timer cardQueryTimer;

    // ============================================
    // LOAN METRICS
    // ============================================
    private Counter loanApplications;
    private Counter loansApproved;
    private Counter loansRejected;
    private Counter loansDisbursed;
    private Counter loansPaidOff;
    private Counter loansDefaulted;
    private Counter loanPayments;
    private DistributionSummary loanAmounts;
    private DistributionSummary loanInterestRates;
    private AtomicLong totalLoansGauge = new AtomicLong(0);
    private AtomicLong activeLoansGauge = new AtomicLong(0);
    private AtomicLong delinquentLoansGauge = new AtomicLong(0);
    private AtomicLong totalOutstandingBalanceGauge = new AtomicLong(0);
    private Timer loanProcessingTimer;
    private Timer loanPaymentTimer;

    // ============================================
    // TRANSACTION METRICS
    // ============================================
    private Counter transactionsCreated;
    private Counter transfersCompleted;
    private Counter purchasesCompleted;
    private Counter withdrawalsCompleted;
    private Counter depositsCompleted;
    private Counter transactionFailures;
    private DistributionSummary transactionAmounts;
    private Timer transactionProcessingTime;
    private AtomicLong totalTransactionVolumeGauge = new AtomicLong(0);
    private AtomicLong totalTransactionValueGauge = new AtomicLong(0);

    // ============================================
    // NOTIFICATION METRICS (Email & Phone)
    // ============================================
    private Counter emailsSent;
    private Counter emailFailures;
    private Counter smsSent;
    private Counter smsFailures;
    private Counter otpGenerated;
    private Counter otpVerified;
    private Counter otpFailed;
    private Timer emailSendingTimer;
    private Timer smsSendingTimer;

    // ============================================
    // SECURITY METRICS
    // ============================================
    private Counter rateLimitHits;
    private Counter invalidTokenAttempts;
    private Counter bruteForceAttempts;
    private Counter accountLockouts;
    private Counter suspiciousActivities;

    // Cache for dynamic counters
    private ConcurrentHashMap<String, Counter> dynamicCounters = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // ============================================
        // USER METRICS INITIALIZATION
        // ============================================
        userRegistrationsSuccess = Counter.builder("banking.user.registrations")
                .tag("status", "success")
                .description("Successful user registrations")
                .register(meterRegistry);

        userRegistrationsFailure = Counter.builder("banking.user.registrations")
                .tag("status", "failure")
                .description("Failed user registrations")
                .register(meterRegistry);

        userLoginsSuccess = Counter.builder("banking.user.logins")
                .tag("status", "success")
                .description("Successful logins")
                .register(meterRegistry);

        userLoginsFailure = Counter.builder("banking.user.logins")
                .tag("status", "failure")
                .description("Failed logins")
                .register(meterRegistry);

        userLogouts = Counter.builder("banking.user.logouts")
                .description("User logouts")
                .register(meterRegistry);

        passwordChangesSuccess = Counter.builder("banking.user.password.changes")
                .tag("status", "success")
                .register(meterRegistry);

        passwordChangesFailure = Counter.builder("banking.user.password.changes")
                .tag("status", "failure")
                .register(meterRegistry);

        passwordResetInitiated = Counter.builder("banking.user.password.reset.initiated")
                .register(meterRegistry);

        passwordResetSuccess = Counter.builder("banking.user.password.reset")
                .tag("status", "success")
                .register(meterRegistry);

        passwordResetFailure = Counter.builder("banking.user.password.reset")
                .tag("status", "failure")
                .register(meterRegistry);

        // User Gauges
        Gauge.builder("banking.user.total", totalUsersGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.user.active", activeUsersGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.user.admins", adminUsersGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.user.managers", managerUsersGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.user.customers", customerUsersGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.user.2fa.enabled", twoFactorEnabledGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.user.email.verified", emailVerifiedGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.user.phone.verified", phoneVerifiedGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.user.locked", lockedUsersGauge, AtomicLong::get).register(meterRegistry);

        // User Timers
        registrationTimer = Timer.builder("banking.user.registration.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);
        loginTimer = Timer.builder("banking.user.login.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);
        passwordResetTimer = Timer.builder("banking.user.password.reset.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);
        emailVerificationTimer = Timer.builder("banking.user.email.verification.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);

        // ============================================
        // ACCOUNT METRICS INITIALIZATION
        // ============================================
        accountsCreated = Counter.builder("banking.accounts.created").register(meterRegistry);
        accountsClosed = Counter.builder("banking.accounts.closed").register(meterRegistry);
        accountCreationFailures = Counter.builder("banking.accounts.creation.failures").register(meterRegistry);
        accountUpdates = Counter.builder("banking.accounts.updated").register(meterRegistry);
        balanceChecks = Counter.builder("banking.accounts.balance.checks").register(meterRegistry);
        accountBalances = DistributionSummary.builder("banking.accounts.balance.amount")
                .publishPercentiles(0.5, 0.75, 0.9, 0.95, 0.99).register(meterRegistry);

        Gauge.builder("banking.accounts.total", totalAccountsGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.accounts.active", activeAccountsGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.accounts.total.balance", totalBalanceGauge, AtomicLong::get).register(meterRegistry);

        accountCreationTimer = Timer.builder("banking.accounts.creation.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);
        accountQueryTimer = Timer.builder("banking.accounts.query.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);

        // ============================================
        // CARD METRICS INITIALIZATION
        // ============================================
        cardsIssued = Counter.builder("banking.cards.issued").register(meterRegistry);
        cardsActivated = Counter.builder("banking.cards.activated").register(meterRegistry);
        cardsBlocked = Counter.builder("banking.cards.blocked").register(meterRegistry);
        cardsCancelled = Counter.builder("banking.cards.cancelled").register(meterRegistry);
        cardsLostStolen = Counter.builder("banking.cards.lost.stolen").register(meterRegistry);
        cardsExpired = Counter.builder("banking.cards.expired").register(meterRegistry);
        cardCreationFailures = Counter.builder("banking.cards.creation.failures").register(meterRegistry);
        cardActivationFailures = Counter.builder("banking.cards.activation.failures").register(meterRegistry);
        pinChangesSuccess = Counter.builder("banking.cards.pin.changes").tag("status", "success").register(meterRegistry);
        pinChangesFailure = Counter.builder("banking.cards.pin.changes").tag("status", "failure").register(meterRegistry);
        cardLimits = DistributionSummary.builder("banking.cards.daily.limit")
                .publishPercentiles(0.5, 0.75, 0.9, 0.95, 0.99).register(meterRegistry);

        Gauge.builder("banking.cards.total", totalCardsGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.cards.active", activeCardsGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.cards.credit", creditCardsGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.cards.debit", debitCardsGauge, AtomicLong::get).register(meterRegistry);

        cardCreationTimer = Timer.builder("banking.cards.creation.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);
        cardActivationTimer = Timer.builder("banking.cards.activation.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);
        cardQueryTimer = Timer.builder("banking.cards.query.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);

        // ============================================
        // LOAN METRICS INITIALIZATION
        // ============================================
        loanApplications = Counter.builder("banking.loans.applications").register(meterRegistry);
        loansApproved = Counter.builder("banking.loans.approved").register(meterRegistry);
        loansRejected = Counter.builder("banking.loans.rejected").register(meterRegistry);
        loansDisbursed = Counter.builder("banking.loans.disbursed").register(meterRegistry);
        loansPaidOff = Counter.builder("banking.loans.paid.off").register(meterRegistry);
        loansDefaulted = Counter.builder("banking.loans.defaulted").register(meterRegistry);
        loanPayments = Counter.builder("banking.loans.payments").register(meterRegistry);
        loanAmounts = DistributionSummary.builder("banking.loans.amount")
                .publishPercentiles(0.5, 0.75, 0.9, 0.95, 0.99).register(meterRegistry);
        loanInterestRates = DistributionSummary.builder("banking.loans.interest.rate")
                .publishPercentiles(0.5, 0.75, 0.9, 0.95, 0.99).register(meterRegistry);

        Gauge.builder("banking.loans.total", totalLoansGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.loans.active", activeLoansGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.loans.delinquent", delinquentLoansGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.loans.outstanding.balance", totalOutstandingBalanceGauge, AtomicLong::get).register(meterRegistry);

        loanProcessingTimer = Timer.builder("banking.loans.processing.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);
        loanPaymentTimer = Timer.builder("banking.loans.payment.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);

        // ============================================
        // TRANSACTION METRICS INITIALIZATION
        // ============================================
        transactionsCreated = Counter.builder("banking.transactions.created").register(meterRegistry);
        transfersCompleted = Counter.builder("banking.transactions.transfers").tag("status", "success").register(meterRegistry);
        purchasesCompleted = Counter.builder("banking.transactions.purchases").tag("status", "success").register(meterRegistry);
        withdrawalsCompleted = Counter.builder("banking.transactions.withdrawals").tag("status", "success").register(meterRegistry);
        depositsCompleted = Counter.builder("banking.transactions.deposits").tag("status", "success").register(meterRegistry);
        transactionFailures = Counter.builder("banking.transactions.failures").register(meterRegistry);
        transactionAmounts = DistributionSummary.builder("banking.transactions.amount")
                .publishPercentiles(0.5, 0.75, 0.9, 0.95, 0.99).register(meterRegistry);
        transactionProcessingTime = Timer.builder("banking.transactions.processing.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);

        Gauge.builder("banking.transactions.volume", totalTransactionVolumeGauge, AtomicLong::get).register(meterRegistry);
        Gauge.builder("banking.transactions.value", totalTransactionValueGauge, AtomicLong::get).register(meterRegistry);

        // ============================================
        // NOTIFICATION METRICS INITIALIZATION
        // ============================================
        emailsSent = Counter.builder("banking.notifications.emails.sent").register(meterRegistry);
        emailFailures = Counter.builder("banking.notifications.emails.failed").register(meterRegistry);
        smsSent = Counter.builder("banking.notifications.sms.sent").register(meterRegistry);
        smsFailures = Counter.builder("banking.notifications.sms.failed").register(meterRegistry);
        otpGenerated = Counter.builder("banking.security.otp.generated").register(meterRegistry);
        otpVerified = Counter.builder("banking.security.otp.verified").tag("status", "success").register(meterRegistry);
        otpFailed = Counter.builder("banking.security.otp.verified").tag("status", "failure").register(meterRegistry);

        emailSendingTimer = Timer.builder("banking.notifications.email.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);
        smsSendingTimer = Timer.builder("banking.notifications.sms.duration")
                .publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);

        // ============================================
        // SECURITY METRICS INITIALIZATION
        // ============================================
        rateLimitHits = Counter.builder("banking.security.rate.limit.hits").register(meterRegistry);
        invalidTokenAttempts = Counter.builder("banking.security.invalid.tokens").register(meterRegistry);
        bruteForceAttempts = Counter.builder("banking.security.brute.force.attempts").register(meterRegistry);
        accountLockouts = Counter.builder("banking.security.account.lockouts").register(meterRegistry);
        suspiciousActivities = Counter.builder("banking.security.suspicious.activities").register(meterRegistry);
    }

    // ============================================
    // USER METRICS METHODS
    // ============================================
    public void recordUserRegistration(boolean success) {
        if (success) userRegistrationsSuccess.increment();
        else userRegistrationsFailure.increment();
    }

    public void recordUserRegistration(boolean success, String errorType) {
        recordUserRegistration(success);
    }

    public void recordUserLogin(boolean success) {
        if (success) userLoginsSuccess.increment();
        else userLoginsFailure.increment();
    }

    public void recordUserLogin(boolean success, String errorType) {
        recordUserLogin(success);
    }

    public void recordUserLogout() { userLogouts.increment(); }
    public void recordLoginByRole(String role) {}
    public void recordPasswordChange(boolean success) {
        if (success) passwordChangesSuccess.increment();
        else passwordChangesFailure.increment();
    }
    public void recordPasswordResetInitiated() { passwordResetInitiated.increment(); }
    public void recordPasswordReset(boolean success) {
        if (success) passwordResetSuccess.increment();
        else passwordResetFailure.increment();
    }
    public void updateUserCounts(long total, long active, long admins) {
        totalUsersGauge.set(total); activeUsersGauge.set(active); adminUsersGauge.set(admins);
    }
    public void updateUserCounts(long total, long active, long admins, long managers, long customers) {
        updateUserCounts(total, active, admins);
        managerUsersGauge.set(managers); customerUsersGauge.set(customers);
    }
    public void updateLockedUsersCount(long count) { lockedUsersGauge.set(count); }
    public void updateEmailVerifiedCount(long count) { emailVerifiedGauge.set(count); }
    public void updatePhoneVerifiedCount(long count) { phoneVerifiedGauge.set(count); }
    public void updateTwoFactorEnabledCount(long count) { twoFactorEnabledGauge.set(count); }

    public Timer.Sample startRegistrationTimer() { return Timer.start(meterRegistry); }
    public void stopRegistrationTimer(Timer.Sample sample) { if (sample != null) sample.stop(registrationTimer); }
    public Timer.Sample startLoginTimer() { return Timer.start(meterRegistry); }
    public void stopLoginTimer(Timer.Sample sample) { if (sample != null) sample.stop(loginTimer); }
    public Timer.Sample startPasswordResetTimer() { return Timer.start(meterRegistry); }
    public void stopPasswordResetTimer(Timer.Sample sample) { if (sample != null) sample.stop(passwordResetTimer); }
    public Timer.Sample startEmailVerificationTimer() { return Timer.start(meterRegistry); }
    public void stopEmailVerificationTimer(Timer.Sample sample) { if (sample != null) sample.stop(emailVerificationTimer); }

    // ============================================
    // ACCOUNT METRICS METHODS
    // ============================================
    public void recordAccountCreated(String accountType, String currency, BigDecimal initialBalance) {
        accountsCreated.increment();
        if (initialBalance != null && initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            accountBalances.record(initialBalance.doubleValue());
        }
    }
    public void recordAccountCreationFailure(String accountType) { accountCreationFailures.increment(); }
    public void recordAccountClosed(String accountType) { accountsClosed.increment(); }
    public void recordAccountUpdate(String accountType) { accountUpdates.increment(); }
    public void recordAccountStatusChange(String oldStatus, String newStatus) {}
    public void recordAccountQuery(String queryType) {}
    public void recordBalanceCheck() { balanceChecks.increment(); }
    public void recordBalanceAmount(BigDecimal amount) { if (amount != null) accountBalances.record(amount.doubleValue()); }
    public void recordTotalBalanceCheck(UUID userId, BigDecimal total) {}
    public void updateAccountMetrics(long totalAccounts, long activeAccounts, BigDecimal totalBalance) {
        totalAccountsGauge.set(totalAccounts); activeAccountsGauge.set(activeAccounts);
        if (totalBalance != null) totalBalanceGauge.set(totalBalance.longValue());
    }
    public Timer.Sample startAccountCreationTimer() { return Timer.start(meterRegistry); }
    public void stopAccountCreationTimer(Timer.Sample sample) { if (sample != null) sample.stop(accountCreationTimer); }
    public Timer.Sample startAccountQueryTimer() { return Timer.start(meterRegistry); }
    public void stopAccountQueryTimer(Timer.Sample sample) { if (sample != null) sample.stop(accountQueryTimer); }

    // ============================================
    // CARD METRICS METHODS
    // ============================================
    public void recordCardCreated(String cardType, String status, BigDecimal dailyLimit) {
        cardsIssued.increment();
        if (dailyLimit != null) cardLimits.record(dailyLimit.doubleValue());
        if ("CREDIT".equals(cardType)) creditCardsGauge.incrementAndGet();
        else debitCardsGauge.incrementAndGet();
    }
    public void recordCardCreationFailure(String cardType, String reason) { cardCreationFailures.increment(); }
    public void recordCardActivation(boolean success) {
        if (success) cardsActivated.increment();
        else cardActivationFailures.increment();
    }
    public void recordCardActivationFailure(String reason) { cardActivationFailures.increment(); }
    public void recordCardBlocked(String cardType) { cardsBlocked.increment(); }
    public void recordCardUnblocked(String cardType) {}
    public void recordCardCancelled(String cardType) { cardsCancelled.increment(); }
    public void recordCardReplaced(String cardType) { cardsLostStolen.increment(); }
    public void recordCardStatusChange(String oldStatus, String newStatus) {}
    public void recordCardQuery(String queryType) {}
    public void recordCardBalanceCheck() {}
    public void recordCardBalanceAmount(BigDecimal amount) {}
    public void recordPinChange(boolean success) {
        if (success) pinChangesSuccess.increment();
        else pinChangesFailure.increment();
    }
    public void recordPinChangeFailure() { pinChangesFailure.increment(); }
    public void updateCardMetrics(long totalCards, long activeCards, long creditCards, long debitCards) {
        totalCardsGauge.set(totalCards); activeCardsGauge.set(activeCards);
        creditCardsGauge.set(creditCards); debitCardsGauge.set(debitCards);
    }
    public Timer.Sample startCardCreationTimer() { return Timer.start(meterRegistry); }
    public void stopCardCreationTimer(Timer.Sample sample) { if (sample != null) sample.stop(cardCreationTimer); }
    public Timer.Sample startCardActivationTimer() { return Timer.start(meterRegistry); }
    public void stopCardActivationTimer(Timer.Sample sample) { if (sample != null) sample.stop(cardActivationTimer); }
    public Timer.Sample startCardQueryTimer() { return Timer.start(meterRegistry); }
    public void stopCardQueryTimer(Timer.Sample sample) { if (sample != null) sample.stop(cardQueryTimer); }

    // ============================================
    // LOAN METRICS METHODS
    // ============================================
    public void recordLoanApplication(BigDecimal amount, BigDecimal interestRate) {
        loanApplications.increment();
        if (amount != null) loanAmounts.record(amount.doubleValue());
        if (interestRate != null) loanInterestRates.record(interestRate.doubleValue());
    }
    public void recordLoanApproval() { loansApproved.increment(); }
    public void recordLoanRejection() { loansRejected.increment(); }
    public void recordLoanDisbursement(BigDecimal amount) {
        loansDisbursed.increment();
        if (amount != null) loanAmounts.record(amount.doubleValue());
    }
    public void recordLoanPayment(BigDecimal amount) {
        loanPayments.increment();
        if (amount != null) transactionAmounts.record(amount.doubleValue());
    }
    public void recordLoanPaidOff() { loansPaidOff.increment(); }
    public void recordLoanDefault() { loansDefaulted.increment(); }
    public void updateLoanMetrics(long totalLoans, long activeLoans, long delinquentLoans, BigDecimal outstandingBalance) {
        totalLoansGauge.set(totalLoans); activeLoansGauge.set(activeLoans);
        delinquentLoansGauge.set(delinquentLoans);
        if (outstandingBalance != null) totalOutstandingBalanceGauge.set(outstandingBalance.longValue());
    }
    public Timer.Sample startLoanProcessingTimer() { return Timer.start(meterRegistry); }
    public void stopLoanProcessingTimer(Timer.Sample sample) { if (sample != null) sample.stop(loanProcessingTimer); }
    public Timer.Sample startLoanPaymentTimer() { return Timer.start(meterRegistry); }
    public void stopLoanPaymentTimer(Timer.Sample sample) { if (sample != null) sample.stop(loanPaymentTimer); }

    // ============================================
    // TRANSACTION METRICS METHODS
    // ============================================
    public void recordTransaction(double amount, String type) {
        transactionsCreated.increment();
        transactionAmounts.record(amount);
        totalTransactionVolumeGauge.incrementAndGet();
        totalTransactionValueGauge.addAndGet((long) amount);

        if ("TRANSFER".equals(type)) transfersCompleted.increment();
        else if ("PURCHASE".equals(type)) purchasesCompleted.increment();
        else if ("ATM_WITHDRAWAL".equals(type)) withdrawalsCompleted.increment();
        else if ("DEPOSIT".equals(type)) depositsCompleted.increment();
    }
    public void recordTransactionFailure() { transactionFailures.increment(); }
    public Timer.Sample startTransactionTimer() { return Timer.start(meterRegistry); }
    public void stopTransactionTimer(Timer.Sample sample) { if (sample != null) sample.stop(transactionProcessingTime); }

    // ============================================
    // NOTIFICATION METRICS METHODS
    // ============================================
    public void recordEmailSent() { emailsSent.increment(); }
    public void recordEmailFailure() { emailFailures.increment(); }
    public void recordSmsSent() { smsSent.increment(); }
    public void recordSmsFailure() { smsFailures.increment(); }
    public void recordOtpGenerated() { otpGenerated.increment(); }
    public void recordOtpVerification(boolean success) {
        if (success) otpVerified.increment();
        else otpFailed.increment();
    }
    public Timer.Sample startEmailSendingTimer() { return Timer.start(meterRegistry); }
    public void stopEmailSendingTimer(Timer.Sample sample) { if (sample != null) sample.stop(emailSendingTimer); }
    public Timer.Sample startSmsSendingTimer() { return Timer.start(meterRegistry); }
    public void stopSmsSendingTimer(Timer.Sample sample) { if (sample != null) sample.stop(smsSendingTimer); }

    // ============================================
    // SECURITY METRICS METHODS
    // ============================================
    public void recordRateLimitHit() { rateLimitHits.increment(); }
    public void recordInvalidToken() { invalidTokenAttempts.increment(); }
    public void recordBruteForceAttempt() { bruteForceAttempts.increment(); }
    public void recordAccountLockout() { accountLockouts.increment(); }
    public void recordSuspiciousActivity() { suspiciousActivities.increment(); }

    // ============================================
    // EMAIL & PHONE (Legacy compatibility)
    // ============================================
    public void recordEmailVerification(boolean success) {}
    public void recordPhoneOtpSent() { otpGenerated.increment(); }
    public void recordPhoneVerification(boolean success) { recordOtpVerification(success); }
    public void recordTwoFactorChange(boolean enabled) {}

    // ============================================
    // GETTER METHODS
    // ============================================
    public long getCurrentTotalUsers() { return totalUsersGauge.get(); }
    public long getCurrentActiveUsers() { return activeUsersGauge.get(); }
    public double getTotalRegistrations() { return userRegistrationsSuccess.count(); }
    public double getTotalLogins() { return userLoginsSuccess.count(); }
    public long getCurrentTotalAccounts() { return totalAccountsGauge.get(); }
    public long getCurrentActiveAccounts() { return activeAccountsGauge.get(); }
    public long getCurrentTotalCards() { return totalCardsGauge.get(); }
    public long getCurrentActiveCards() { return activeCardsGauge.get(); }
    public long getCurrentTotalLoans() { return totalLoansGauge.get(); }
    public long getCurrentActiveLoans() { return activeLoansGauge.get(); }

    // ============================================
    // GENERIC METHODS
    // ============================================
    public Counter getOrCreateCounter(String name, String... tags) {
        String key = name + String.join("", tags);
        return dynamicCounters.computeIfAbsent(key, k -> Counter.builder(name).tags(tags).register(meterRegistry));
    }
    public Timer getOrCreateTimer(String name, String... tags) {
        return Timer.builder(name).tags(tags).publishPercentiles(0.5, 0.95, 0.99).register(meterRegistry);
    }
    public DistributionSummary getOrCreateDistributionSummary(String name, String... tags) {
        return DistributionSummary.builder(name).tags(tags).publishPercentiles(0.5, 0.75, 0.9, 0.95, 0.99).register(meterRegistry);
    }

    public void recordCardUpdate(String name) {
    }
}