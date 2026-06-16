// src/main/java/com/cognitive/banking/security/RolePrivileges.java
package com.cognitive.banking.security;

import java.util.Set;

public enum RolePrivileges {

    ROLE_CUSTOMER(Set.of(
            Permission.USER_READ,
            Permission.ACCOUNT_READ,
            Permission.TRANSACTION_CREATE,
            Permission.TRANSACTION_READ,
            Permission.CARD_READ,
            Permission.LOAN_READ
    )),

    ROLE_ACCOUNT_MANAGER(Set.of(
            Permission.USER_READ,
            Permission.USER_UPDATE,
            Permission.ACCOUNT_CREATE,
            Permission.ACCOUNT_READ,
            Permission.ACCOUNT_UPDATE,
            Permission.ACCOUNT_FREEZE,
            Permission.ACCOUNT_UNFREEZE,
            Permission.TRANSACTION_READ,
            Permission.TRANSACTION_APPROVE,
            Permission.CARD_CREATE,
            Permission.CARD_READ,
            Permission.CARD_UPDATE,
            Permission.CARD_BLOCK,
            Permission.CARD_UNBLOCK,
            Permission.LOAN_CREATE,
            Permission.LOAN_READ
    )),

    ROLE_LOAN_OFFICER(Set.of(
            Permission.USER_READ,
            Permission.LOAN_CREATE,
            Permission.LOAN_READ,
            Permission.LOAN_APPROVE,
            Permission.LOAN_REJECT
    )),

    ROLE_ADMIN(Set.of(
            Permission.USER_CREATE,
            Permission.USER_READ,
            Permission.USER_UPDATE,
            Permission.USER_DELETE,
            Permission.USER_ACTIVATE,
            Permission.USER_DEACTIVATE,
            Permission.ACCOUNT_CREATE,
            Permission.ACCOUNT_READ,
            Permission.ACCOUNT_UPDATE,
            Permission.ACCOUNT_DELETE,
            Permission.TRANSACTION_CREATE,
            Permission.TRANSACTION_READ,
            Permission.TRANSACTION_APPROVE,
            Permission.TRANSACTION_REVERSE,
            Permission.CARD_CREATE,
            Permission.CARD_READ,
            Permission.CARD_UPDATE,
            Permission.CARD_BLOCK,
            Permission.LOAN_CREATE,
            Permission.LOAN_READ,
            Permission.LOAN_APPROVE,
            Permission.ADMIN_ACCESS,
            Permission.AUDIT_READ,
            Permission.METRICS_READ,
            Permission.SYSTEM_CONFIG
    )),

    ROLE_AUDITOR(Set.of(
            Permission.USER_READ,
            Permission.ACCOUNT_READ,
            Permission.TRANSACTION_READ,
            Permission.CARD_READ,
            Permission.LOAN_READ,
            Permission.AUDIT_READ,
            Permission.METRICS_READ
    ));

    private final Set<Permission> privileges;

    RolePrivileges(Set<Permission> privileges) {
        this.privileges = privileges;
    }

    public Set<Permission> getPrivileges() {
        return privileges;
    }
}