package com.cognitive.banking.monitoring.metrics;

public final class MetricTags {

    // ============================================
    // STATUS TAGS
    // ============================================
    public static final String STATUS = "status";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILURE = "failure";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";
    public static final String STATUS_BLOCKED = "blocked";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_DECLINED = "declined";
    public static final String STATUS_REVERSED = "reversed";

    // ============================================
    // ACTION TAGS
    // ============================================
    public static final String ACTION = "action";
    public static final String ACTION_ENABLED = "enabled";
    public static final String ACTION_DISABLED = "disabled";
    public static final String ACTION_CREATE = "create";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_ACTIVATE = "activate";
    public static final String ACTION_BLOCK = "block";
    public static final String ACTION_UNBLOCK = "unblock";
    public static final String ACTION_CANCEL = "cancel";
    public static final String ACTION_REPLACE = "replace";

    // ============================================
    // ROLE TAGS
    // ============================================
    public static final String ROLE = "role";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    // ============================================
    // TYPE TAGS
    // ============================================
    public static final String TYPE = "type";
    public static final String TYPE_CREDIT = "CREDIT";
    public static final String TYPE_DEBIT = "DEBIT";
    public static final String TYPE_TRANSFER = "TRANSFER";
    public static final String TYPE_PURCHASE = "PURCHASE";
    public static final String TYPE_ATM_WITHDRAWAL = "ATM_WITHDRAWAL";
    public static final String TYPE_DEPOSIT = "DEPOSIT";
    public static final String TYPE_FEE = "FEE";
    public static final String TYPE_INTEREST = "INTEREST";
    public static final String TYPE_REFUND = "REFUND";

    // ============================================
    // CARD TYPE TAGS
    // ============================================
    public static final String CARD_TYPE = "card_type";
    public static final String CARD_TYPE_CREDIT = "CREDIT";
    public static final String CARD_TYPE_DEBIT = "DEBIT";

    // ============================================
    // ACCOUNT TYPE TAGS
    // ============================================
    public static final String ACCOUNT_TYPE = "account_type";
    public static final String ACCOUNT_TYPE_CHECKING = "CHECKING";
    public static final String ACCOUNT_TYPE_SAVINGS = "SAVINGS";

    // ============================================
    // LOAN TYPE TAGS
    // ============================================
    public static final String LOAN_TYPE = "loan_type";
    public static final String LOAN_TYPE_PERSONAL = "PERSONAL";
    public static final String LOAN_TYPE_HOME = "HOME";
    public static final String LOAN_TYPE_AUTO = "AUTO";

    // ============================================
    // LOAN STATUS TAGS
    // ============================================
    public static final String LOAN_STATUS = "loan_status";
    public static final String LOAN_STATUS_PENDING = "PENDING";
    public static final String LOAN_STATUS_APPROVED = "APPROVED";
    public static final String LOAN_STATUS_ACTIVE = "ACTIVE";
    public static final String LOAN_STATUS_REJECTED = "REJECTED";
    public static final String LOAN_STATUS_PAID_OFF = "PAID_OFF";
    public static final String LOAN_STATUS_DEFAULTED = "DEFAULTED";

    // ============================================
    // COMPONENT TAGS
    // ============================================
    public static final String COMPONENT = "component";
    public static final String COMPONENT_API = "api";
    public static final String COMPONENT_DB = "database";
    public static final String COMPONENT_CACHE = "cache";
    public static final String COMPONENT_AUTH = "authentication";
    public static final String COMPONENT_NOTIFICATION = "notification";
    public static final String COMPONENT_SCHEDULER = "scheduler";

    // ============================================
    // QUERY TYPE TAGS
    // ============================================
    public static final String QUERY_TYPE = "query_type";
    public static final String QUERY_BY_ID = "by_id";
    public static final String QUERY_BY_NUMBER = "by_number";
    public static final String QUERY_BY_USER_ID = "by_user_id";
    public static final String QUERY_BY_ACCOUNT_ID = "by_account_id";
    public static final String QUERY_ALL = "all";
    public static final String QUERY_MY_ACCOUNTS = "my_accounts";

    // ============================================
    // CURRENCY TAGS
    // ============================================
    public static final String CURRENCY = "currency";
    public static final String CURRENCY_USD = "USD";
    public static final String CURRENCY_EUR = "EUR";
    public static final String CURRENCY_GBP = "GBP";

    // ============================================
    // ERROR TYPE TAGS
    // ============================================
    public static final String ERROR_TYPE = "error.type";
    public static final String ERROR_INVALID_CREDENTIALS = "invalid_credentials";
    public static final String ERROR_USER_NOT_FOUND = "user_not_found";
    public static final String ERROR_DUPLICATE_EMAIL = "duplicate_email";
    public static final String ERROR_INSUFFICIENT_FUNDS = "insufficient_funds";
    public static final String ERROR_ACCOUNT_LOCKED = "account_locked";
    public static final String ERROR_CARD_EXPIRED = "card_expired";
    public static final String ERROR_INVALID_PIN = "invalid_pin";
    public static final String ERROR_INVALID_TOKEN = "invalid_token";
    public static final String ERROR_ACCOUNT_MISMATCH = "account_mismatch";

    // ============================================
    // OLD STATUS TAGS (Legacy compatibility)
    // ============================================
    public static final String OLD_STATUS = "old_status";
    public static final String NEW_STATUS = "new_status";

    private MetricTags() {} // Prevent instantiation
}