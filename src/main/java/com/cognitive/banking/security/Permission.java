package com.cognitive.banking.security;

public enum Permission {
    // User Management
    USER_CREATE("user:create"),
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),
    USER_ACTIVATE("user:activate"),
    USER_DEACTIVATE("user:deactivate"),

    // Account Management
    ACCOUNT_CREATE("account:create"),
    ACCOUNT_READ("account:read"),
    ACCOUNT_UPDATE("account:update"),
    ACCOUNT_DELETE("account:delete"),
    ACCOUNT_FREEZE("account:freeze"),
    ACCOUNT_UNFREEZE("account:unfreeze"),

    // Transaction Management
    TRANSACTION_CREATE("transaction:create"),
    TRANSACTION_READ("transaction:read"),
    TRANSACTION_APPROVE("transaction:approve"),
    TRANSACTION_REVERSE("transaction:reverse"),

    // Card Management
    CARD_CREATE("card:create"),
    CARD_READ("card:read"),
    CARD_UPDATE("card:update"),
    CARD_BLOCK("card:block"),
    CARD_UNBLOCK("card:unblock"),

    // Loan Management
    LOAN_CREATE("loan:create"),
    LOAN_READ("loan:read"),
    LOAN_APPROVE("loan:approve"),
    LOAN_REJECT("loan:reject"),

    // Admin & System
    ADMIN_ACCESS("admin:access"),
    AUDIT_READ("audit:read"),
    METRICS_READ("metrics:read"),
    SYSTEM_CONFIG("system:config");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}