-- V6__create_transactions_table.sql
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id UUID PRIMARY KEY,
    transaction_reference VARCHAR(100) NOT NULL UNIQUE,
    transaction_type VARCHAR(50) NOT NULL,
    transaction_status VARCHAR(20) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    description TEXT,
    merchant_name VARCHAR(100),
    merchant_category VARCHAR(50),
    from_account_id UUID,
    to_account_id UUID,
    card_id UUID,
    balance_after DECIMAL(19,2),
    available_balance_after DECIMAL(19,2),
    transaction_date TIMESTAMP NOT NULL,
    posted_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create all indexes (non-partial)
CREATE INDEX IF NOT EXISTS idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_to_account ON transactions(to_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_card_id ON transactions(card_id);
CREATE INDEX IF NOT EXISTS idx_transactions_transaction_type ON transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_transactions_transaction_status ON transactions(transaction_status);
CREATE INDEX IF NOT EXISTS idx_transactions_transaction_date ON transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_transaction_reference ON transactions(transaction_reference);
CREATE INDEX IF NOT EXISTS idx_transactions_from_date ON transactions(from_account_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_status_date ON transactions(transaction_status, transaction_date);

-- Add foreign key constraints
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_from_account
    FOREIGN KEY (from_account_id) REFERENCES accounts(account_id) ON DELETE SET NULL;

ALTER TABLE transactions ADD CONSTRAINT fk_transactions_to_account
    FOREIGN KEY (to_account_id) REFERENCES accounts(account_id) ON DELETE SET NULL;

ALTER TABLE transactions ADD CONSTRAINT fk_transactions_card
    FOREIGN KEY (card_id) REFERENCES cards(card_id) ON DELETE SET NULL;