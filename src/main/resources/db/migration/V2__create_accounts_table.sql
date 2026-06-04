-- V2__create_accounts_table.sql
-- Baseline migration for the accounts table

CREATE TABLE accounts (
    account_id UUID PRIMARY KEY,                   -- matches JpaRepository<Account, UUID>
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    account_number VARCHAR(30) NOT NULL UNIQUE,    -- supports findByAccountNumber
    routing_number VARCHAR(20) NOT NULL,           -- supports findByAccountAndRoutingNumber
    balance NUMERIC(15,2) NOT NULL DEFAULT 0,      -- supports getTotalBalanceByUserId
    account_type VARCHAR(20) NOT NULL,             -- maps to AccountType enum
    account_status VARCHAR(20) NOT NULL,           -- maps to AccountStatus enum
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Helpful indexes for queries in AccountRepository
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_routing_number ON accounts(routing_number);
CREATE INDEX idx_accounts_status ON accounts(account_status);
CREATE INDEX idx_accounts_type ON accounts(account_type);
