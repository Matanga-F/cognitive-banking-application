-- V6__create_transactions_table.sql
-- Baseline migration for the transactions table

CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY,                       -- matches JpaRepository<Transaction, UUID>
    from_account UUID REFERENCES accounts(account_id) ON DELETE SET NULL,
    to_account UUID REFERENCES accounts(account_id) ON DELETE SET NULL,
    card_id UUID REFERENCES cards(card_id) ON DELETE SET NULL,
    amount NUMERIC(15,2) NOT NULL,                         -- supports debit/credit sums
    transaction_type VARCHAR(50) NOT NULL,                 -- maps to TransactionType enum
    transaction_status VARCHAR(20) NOT NULL,               -- maps to TransactionStatus enum
    transaction_date TIMESTAMP NOT NULL,                   -- supports date range queries
    transaction_reference VARCHAR(100) UNIQUE,             -- supports findByTransactionReference
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Helpful indexes for queries in TransactionRepository
CREATE INDEX idx_transactions_from_account ON transactions(from_account);
CREATE INDEX idx_transactions_to_account ON transactions(to_account);
CREATE INDEX idx_transactions_card_id ON transactions(card_id);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_status ON transactions(transaction_status);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_reference ON transactions(transaction_reference);
