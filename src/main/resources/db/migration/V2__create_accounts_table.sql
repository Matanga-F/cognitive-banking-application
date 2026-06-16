-- V2__create_accounts_table.sql
-- Baseline migration for the accounts table

CREATE TABLE IF NOT EXISTS accounts (
    -- Primary Key
    account_id UUID PRIMARY KEY,                    -- matches JpaRepository<Account, UUID>

    -- User Relationship
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    -- Account Identification
    account_number VARCHAR(30) NOT NULL UNIQUE,     -- supports findByAccountNumber
    routing_number VARCHAR(20) NOT NULL,            -- supports findByAccountAndRoutingNumber

    -- Account Type & Status
    account_type VARCHAR(20) NOT NULL,              -- maps to AccountType enum (CHECKING, SAVINGS, etc.)
    account_status VARCHAR(20) NOT NULL,            -- maps to AccountStatus enum (ACTIVE, INACTIVE, CLOSED, FROZEN)

    -- Balance Information
    balance NUMERIC(15,2) NOT NULL DEFAULT 0,       -- current balance
    available_balance NUMERIC(15,2) NOT NULL DEFAULT 0, -- available balance (balance - holds)
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',     -- currency code (USD, EUR, GBP, etc.)

    -- Account Features
    overdraft_limit NUMERIC(15,2) DEFAULT 0,        -- overdraft limit for checking accounts
    interest_rate NUMERIC(5,4) DEFAULT 0,           -- interest rate for savings accounts
    minimum_balance_required NUMERIC(15,2) DEFAULT 0, -- minimum balance requirement
    maintenance_fee NUMERIC(15,2) DEFAULT 0,        -- monthly maintenance fee
    is_overdraft_protection_enabled BOOLEAN DEFAULT FALSE, -- overdraft protection flag

    -- Freeze/Block Information
    is_frozen BOOLEAN DEFAULT FALSE,                -- account frozen flag
    freeze_reason VARCHAR(255),                     -- reason for freezing
    frozen_at TIMESTAMP,                            -- when account was frozen

    -- Closure Information
    closed_at TIMESTAMP,                            -- when account was closed
    closed_reason VARCHAR(255),                     -- reason for closure

    -- Activity Tracking
    last_transaction_date TIMESTAMP,                -- last transaction date
    last_interest_calculation_date TIMESTAMP,       -- last time interest was calculated
    dormant_notification_sent BOOLEAN DEFAULT FALSE, -- notification sent for dormant account

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- last update timestamp
    created_by VARCHAR(100),                        -- user who created the account
    updated_by VARCHAR(100),                        -- user who last updated the account
    version BIGINT DEFAULT 0                        -- optimistic locking version
);

-- Helpful indexes for queries in AccountRepository
CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_account_number ON accounts(account_number);
CREATE INDEX IF NOT EXISTS idx_accounts_routing_number ON accounts(routing_number);
CREATE INDEX IF NOT EXISTS idx_accounts_status ON accounts(account_status);
CREATE INDEX IF NOT EXISTS idx_accounts_type ON accounts(account_type);
CREATE INDEX IF NOT EXISTS idx_accounts_currency ON accounts(currency);
CREATE INDEX IF NOT EXISTS idx_accounts_balance ON accounts(balance);
CREATE INDEX IF NOT EXISTS idx_accounts_created_at ON accounts(created_at);
CREATE INDEX IF NOT EXISTS idx_accounts_user_status ON accounts(user_id, account_status);
CREATE INDEX IF NOT EXISTS idx_accounts_user_type ON accounts(user_id, account_type);

-- Optional: Create a composite index for common queries
CREATE INDEX IF NOT EXISTS idx_accounts_user_status_type ON accounts(user_id, account_status, account_type);

-- Comment on table and columns for documentation
COMMENT ON TABLE accounts IS 'Bank accounts table for managing user accounts';
COMMENT ON COLUMN accounts.account_id IS 'Unique identifier for the account';
COMMENT ON COLUMN accounts.user_id IS 'Foreign key reference to the user who owns this account';
COMMENT ON COLUMN accounts.account_number IS 'Unique account number (masked in responses)';
COMMENT ON COLUMN accounts.routing_number IS 'Bank routing number';
COMMENT ON COLUMN accounts.account_type IS 'Type of account: CHECKING, SAVINGS, etc.';
COMMENT ON COLUMN accounts.account_status IS 'Status: ACTIVE, INACTIVE, CLOSED, FROZEN';
COMMENT ON COLUMN accounts.balance IS 'Current account balance';
COMMENT ON COLUMN accounts.available_balance IS 'Available balance (balance minus holds)';
COMMENT ON COLUMN accounts.currency IS 'Currency code (USD, EUR, GBP)';
COMMENT ON COLUMN accounts.overdraft_limit IS 'Overdraft limit for checking accounts';
COMMENT ON COLUMN accounts.interest_rate IS 'Interest rate for savings accounts';
COMMENT ON COLUMN accounts.is_frozen IS 'Flag indicating if account is frozen';
COMMENT ON COLUMN accounts.version IS 'Optimistic locking version for concurrent updates';