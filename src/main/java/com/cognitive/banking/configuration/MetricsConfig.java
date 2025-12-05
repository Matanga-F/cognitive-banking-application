package com.cognitive.banking.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.binder.db.PostgreSQLDatabaseMetrics;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Configuration
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    private final RedisConnectionFactory redisConnectionFactory;

    // Domain-specific metrics
    private Counter userCreationCounter;
    private Counter transactionCounter;
    private Counter loanApplicationCounter;
    private Counter cardCreationCounter;
    private Counter accountCreationCounter;

    // HTTP metrics
    private Counter httpRequestsTotal;

    // Business metrics
    private Gauge activeUsersGauge;
    private Gauge totalAccountsGauge;
    private Gauge activeLoansGauge;
    private Gauge totalBalanceGauge;

    // Performance metrics
    private Timer userServiceTimer;
    private Timer transactionServiceTimer;
    private Timer loanServiceTimer;
    private Timer accountServiceTimer;

    // Financial metrics
    private DistributionSummary transactionAmountSummary;
    private DistributionSummary loanAmountSummary;
    private DistributionSummary accountBalanceSummary;

    public MetricsConfig(MeterRegistry meterRegistry, RedisConnectionFactory redisConnectionFactory) {
        this.meterRegistry = meterRegistry;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @PostConstruct
    public void init() {
        // Initialize system metrics
        new JvmMemoryMetrics().bindTo(meterRegistry);
        new JvmGcMetrics().bindTo(meterRegistry);
        new JvmThreadMetrics().bindTo(meterRegistry);
        new ProcessorMetrics().bindTo(meterRegistry);
        new UptimeMetrics().bindTo(meterRegistry);

        // Initialize database metrics
        // PostgreSQL metrics will be added when we have DataSource

        // Initialize Redis metrics
//        new RedisCacheMetrics(redisConnectionFactory, "redis").bindTo(meterRegistry);

        // ================= HTTP Metrics =================
        httpRequestsTotal = Counter.builder("http_requests_total")
                .description("Total HTTP requests by method and endpoint")
                .tag("application", "cognitive-banking")
                .register(meterRegistry);

        // ================= Domain Metrics - Counters =================
        userCreationCounter = Counter.builder("user_creation_total")
                .description("Total users created")
                .tag("domain", "users")
                .register(meterRegistry);

        transactionCounter = Counter.builder("transaction_total")
                .description("Total transactions processed")
                .tags("domain", "transactions")
                .register(meterRegistry);

        loanApplicationCounter = Counter.builder("loan_application_total")
                .description("Total loan applications")
                .tag("domain", "loans")
                .register(meterRegistry);

        cardCreationCounter = Counter.builder("card_creation_total")
                .description("Total cards created")
                .tag("domain", "cards")
                .register(meterRegistry);

        accountCreationCounter = Counter.builder("account_creation_total")
                .description("Total accounts created")
                .tag("domain", "accounts")
                .register(meterRegistry);

        // ================= Business Metrics - Gauges =================
        // These will be updated by service methods
        activeUsersGauge = Gauge.builder("active_users_count", () -> 0)
                .description("Current number of active users")
                .tag("domain", "users")
                .register(meterRegistry);

        totalAccountsGauge = Gauge.builder("total_accounts_count", () -> 0)
                .description("Current number of total accounts")
                .tag("domain", "accounts")
                .register(meterRegistry);

        activeLoansGauge = Gauge.builder("active_loans_count", () -> 0)
                .description("Current number of active loans")
                .tag("domain", "loans")
                .register(meterRegistry);

        totalBalanceGauge = Gauge.builder("total_balance_amount", () -> 0.0)
                .description("Total balance across all accounts")
                .tag("domain", "accounts")
                .register(meterRegistry);

        // ================= Performance Metrics - Timers =================
        userServiceTimer = Timer.builder("user_service_duration_seconds")
                .description("User service method execution time")
                .tag("service", "user")
                .register(meterRegistry);

        transactionServiceTimer = Timer.builder("transaction_service_duration_seconds")
                .description("Transaction service method execution time")
                .tag("service", "transaction")
                .register(meterRegistry);

        loanServiceTimer = Timer.builder("loan_service_duration_seconds")
                .description("Loan service method execution time")
                .tag("service", "loan")
                .register(meterRegistry);

        accountServiceTimer = Timer.builder("account_service_duration_seconds")
                .description("Account service method execution time")
                .tag("service", "account")
                .register(meterRegistry);

        // ================= Financial Metrics - Distribution Summaries =================
        transactionAmountSummary = DistributionSummary.builder("transaction_amount_distribution")
                .description("Distribution of transaction amounts")
                .baseUnit("currency")
                .tag("domain", "transactions")
                .register(meterRegistry);

        loanAmountSummary = DistributionSummary.builder("loan_amount_distribution")
                .description("Distribution of loan amounts")
                .baseUnit("currency")
                .tag("domain", "loans")
                .register(meterRegistry);

        accountBalanceSummary = DistributionSummary.builder("account_balance_distribution")
                .description("Distribution of account balances")
                .baseUnit("currency")
                .tag("domain", "accounts")
                .register(meterRegistry);
    }

    // ================= Getters for metrics =================
    public Counter getUserCreationCounter() {
        return userCreationCounter;
    }

    public Counter getTransactionCounter() {
        return transactionCounter;
    }

    public Counter getLoanApplicationCounter() {
        return loanApplicationCounter;
    }

    public Counter getCardCreationCounter() {
        return cardCreationCounter;
    }

    public Counter getAccountCreationCounter() {
        return accountCreationCounter;
    }

    public Counter getHttpRequestsTotal() {
        return httpRequestsTotal;
    }

    public Gauge getActiveUsersGauge() {
        return activeUsersGauge;
    }

    public Gauge getTotalAccountsGauge() {
        return totalAccountsGauge;
    }

    public Gauge getActiveLoansGauge() {
        return activeLoansGauge;
    }

    public Gauge getTotalBalanceGauge() {
        return totalBalanceGauge;
    }

    public Timer getUserServiceTimer() {
        return userServiceTimer;
    }

    public Timer getTransactionServiceTimer() {
        return transactionServiceTimer;
    }

    public Timer getLoanServiceTimer() {
        return loanServiceTimer;
    }

    public Timer getAccountServiceTimer() {
        return accountServiceTimer;
    }

    public DistributionSummary getTransactionAmountSummary() {
        return transactionAmountSummary;
    }

    public DistributionSummary getLoanAmountSummary() {
        return loanAmountSummary;
    }

    public DistributionSummary getAccountBalanceSummary() {
        return accountBalanceSummary;
    }
}