package com.cognitive.banking.monitoring.metrics;

public final class MetricNames {

    // ============================================
    // USER METRICS
    // ============================================
    public static final String USER_REGISTRATIONS = "banking.user.registrations";
    public static final String USER_LOGINS = "banking.user.logins";
    public static final String USER_LOGOUTS = "banking.user.logouts";
    public static final String USER_TOTAL = "banking.user.total";
    public static final String USER_ACTIVE = "banking.user.active";
    public static final String USER_LOCKED = "banking.user.locked";
    public static final String USER_PASSWORD_CHANGES = "banking.user.password.changes";
    public static final String USER_PASSWORD_RESET = "banking.user.password.reset";
    public static final String USER_PASSWORD_RESET_INITIATED = "banking.user.password.reset.initiated";

    // User by Role
    public static final String USER_ADMINS = "banking.user.admins";
    public static final String USER_MANAGERS = "banking.user.managers";
    public static final String USER_CUSTOMERS = "banking.user.customers";
    public static final String USER_LOGINS_BY_ROLE = "banking.user.logins.by.role";

    // User Verification
    public static final String USER_EMAIL_VERIFIED = "banking.user.email.verified";
    public static final String USER_PHONE_VERIFIED = "banking.user.phone.verified";
    public static final String USER_2FA_ENABLED = "banking.user.2fa.enabled";
    public static final String USER_EMAIL_VERIFICATIONS = "banking.user.email.verifications";
    public static final String USER_PHONE_VERIFICATIONS = "banking.user.phone.verifications";
    public static final String USER_PHONE_OTP_SENT = "banking.user.phone.otp.sent";

    // User Errors
    public static final String USER_REGISTRATION_ERRORS = "banking.user.registration.errors";
    public static final String USER_LOGIN_ERRORS = "banking.user.login.errors";

    // User Timers
    public static final String USER_REGISTRATION_DURATION = "banking.user.registration.duration";
    public static final String USER_LOGIN_DURATION = "banking.user.login.duration";
    public static final String USER_PASSWORD_RESET_DURATION = "banking.user.password.reset.duration";
    public static final String USER_EMAIL_VERIFICATION_DURATION = "banking.user.email.verification.duration";

    // ============================================
    // ACCOUNT METRICS
    // ============================================
    public static final String ACCOUNTS_CREATED = "banking.accounts.created";
    public static final String ACCOUNTS_CLOSED = "banking.accounts.closed";
    public static final String ACCOUNTS_UPDATED = "banking.accounts.updated";
    public static final String ACCOUNTS_CREATION_FAILURES = "banking.accounts.creation.failures";
    public static final String ACCOUNTS_CREATED_BY_TYPE = "banking.accounts.created.by.type";
    public static final String ACCOUNTS_CLOSED_BY_TYPE = "banking.accounts.closed.by.type";
    public static final String ACCOUNTS_UPDATES_BY_TYPE = "banking.accounts.updates.by.type";
    public static final String ACCOUNTS_CREATION_FAILURES_BY_TYPE = "banking.accounts.creation.failures.by.type";
    public static final String ACCOUNTS_STATUS_CHANGES = "banking.accounts.status.changes";
    public static final String ACCOUNTS_QUERIES = "banking.accounts.queries";
    public static final String ACCOUNTS_BALANCE_CHECKS = "banking.accounts.balance.checks";
    public static final String ACCOUNTS_BALANCE_AMOUNT = "banking.accounts.balance.amount";
    public static final String ACCOUNTS_TOTAL_BALANCE_CHECKS = "banking.accounts.total.balance.checks";

    // Account Gauges
    public static final String ACCOUNTS_TOTAL = "banking.accounts.total";
    public static final String ACCOUNTS_ACTIVE = "banking.accounts.active";
    public static final String ACCOUNTS_TOTAL_BALANCE = "banking.accounts.total.balance";

    // Account Timers
    public static final String ACCOUNTS_CREATION_DURATION = "banking.accounts.creation.duration";
    public static final String ACCOUNTS_QUERY_DURATION = "banking.accounts.query.duration";

    // ============================================
    // CARD METRICS
    // ============================================
    public static final String CARDS_ISSUED = "banking.cards.issued";
    public static final String CARDS_ACTIVATED = "banking.cards.activated";
    public static final String CARDS_BLOCKED = "banking.cards.blocked";
    public static final String CARDS_CANCELLED = "banking.cards.cancelled";
    public static final String CARDS_LOST_STOLEN = "banking.cards.lost.stolen";
    public static final String CARDS_EXPIRED = "banking.cards.expired";
    public static final String CARDS_CREATION_FAILURES = "banking.cards.creation.failures";
    public static final String CARDS_ACTIVATION_FAILURES = "banking.cards.activation.failures";
    public static final String CARDS_PIN_CHANGES = "banking.cards.pin.changes";
    public static final String CARDS_DAILY_LIMIT = "banking.cards.daily.limit";
    public static final String CARDS_QUERIES = "banking.cards.queries";
    public static final String CARDS_BALANCE_CHECKS = "banking.cards.balance.checks";

    // Card Gauges
    public static final String CARDS_TOTAL = "banking.cards.total";
    public static final String CARDS_ACTIVE = "banking.cards.active";
    public static final String CARDS_CREDIT = "banking.cards.credit";
    public static final String CARDS_DEBIT = "banking.cards.debit";

    // Card Timers
    public static final String CARDS_CREATION_DURATION = "banking.cards.creation.duration";
    public static final String CARDS_ACTIVATION_DURATION = "banking.cards.activation.duration";
    public static final String CARDS_QUERY_DURATION = "banking.cards.query.duration";

    // ============================================
    // LOAN METRICS
    // ============================================
    public static final String LOANS_APPLICATIONS = "banking.loans.applications";
    public static final String LOANS_APPROVED = "banking.loans.approved";
    public static final String LOANS_REJECTED = "banking.loans.rejected";
    public static final String LOANS_DISBURSED = "banking.loans.disbursed";
    public static final String LOANS_PAID_OFF = "banking.loans.paid.off";
    public static final String LOANS_DEFAULTED = "banking.loans.defaulted";
    public static final String LOANS_PAYMENTS = "banking.loans.payments";
    public static final String LOANS_AMOUNT = "banking.loans.amount";
    public static final String LOANS_INTEREST_RATE = "banking.loans.interest.rate";

    // Loan Gauges
    public static final String LOANS_TOTAL = "banking.loans.total";
    public static final String LOANS_ACTIVE = "banking.loans.active";
    public static final String LOANS_DELINQUENT = "banking.loans.delinquent";
    public static final String LOANS_OUTSTANDING_BALANCE = "banking.loans.outstanding.balance";

    // Loan Timers
    public static final String LOANS_PROCESSING_DURATION = "banking.loans.processing.duration";
    public static final String LOANS_PAYMENT_DURATION = "banking.loans.payment.duration";

    // ============================================
    // TRANSACTION METRICS
    // ============================================
    public static final String TRANSACTIONS_CREATED = "banking.transactions.created";
    public static final String TRANSACTIONS_TRANSFERS = "banking.transactions.transfers";
    public static final String TRANSACTIONS_PURCHASES = "banking.transactions.purchases";
    public static final String TRANSACTIONS_WITHDRAWALS = "banking.transactions.withdrawals";
    public static final String TRANSACTIONS_DEPOSITS = "banking.transactions.deposits";
    public static final String TRANSACTIONS_FAILURES = "banking.transactions.failures";
    public static final String TRANSACTIONS_AMOUNT = "banking.transactions.amount";
    public static final String TRANSACTIONS_BY_TYPE = "banking.transactions.by.type";

    // Transaction Gauges
    public static final String TRANSACTIONS_VOLUME = "banking.transactions.volume";
    public static final String TRANSACTIONS_VALUE = "banking.transactions.value";

    // Transaction Timers
    public static final String TRANSACTIONS_PROCESSING_DURATION = "banking.transactions.processing.duration";

    // ============================================
    // NOTIFICATION METRICS (Email & SMS)
    // ============================================
    public static final String NOTIFICATIONS_EMAILS_SENT = "banking.notifications.emails.sent";
    public static final String NOTIFICATIONS_EMAILS_FAILED = "banking.notifications.emails.failed";
    public static final String NOTIFICATIONS_SMS_SENT = "banking.notifications.sms.sent";
    public static final String NOTIFICATIONS_SMS_FAILED = "banking.notifications.sms.failed";
    public static final String NOTIFICATIONS_EMAIL_DURATION = "banking.notifications.email.duration";
    public static final String NOTIFICATIONS_SMS_DURATION = "banking.notifications.sms.duration";

    // ============================================
    // SECURITY METRICS
    // ============================================
    public static final String SECURITY_OTP_GENERATED = "banking.security.otp.generated";
    public static final String SECURITY_OTP_VERIFIED = "banking.security.otp.verified";
    public static final String SECURITY_RATE_LIMIT_HITS = "banking.security.rate.limit.hits";
    public static final String SECURITY_INVALID_TOKENS = "banking.security.invalid.tokens";
    public static final String SECURITY_BRUTE_FORCE_ATTEMPTS = "banking.security.brute.force.attempts";
    public static final String SECURITY_ACCOUNT_LOCKOUTS = "banking.security.account.lockouts";
    public static final String SECURITY_SUSPICIOUS_ACTIVITIES = "banking.security.suspicious.activities";

    // ============================================
    // PERFORMANCE METRICS
    // ============================================
    public static final String API_RESPONSE_TIME = "banking.api.response.time";
    public static final String DB_QUERY_TIME = "banking.db.query.time";
    public static final String CACHE_HIT_RATIO = "banking.cache.hit.ratio";

    private MetricNames() {} // Prevent instantiation
}