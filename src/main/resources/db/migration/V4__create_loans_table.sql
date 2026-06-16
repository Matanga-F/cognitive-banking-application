-- V4__create_loans_table.sql
-- Create loans table

CREATE TABLE IF NOT EXISTS loans (
    loan_id UUID PRIMARY KEY,
    loan_number VARCHAR(20) NOT NULL UNIQUE,
    loan_type VARCHAR(30) NOT NULL,
    loan_status VARCHAR(20) NOT NULL,
    principal_amount DECIMAL(19,2) NOT NULL,
    interest_rate DECIMAL(5,4) NOT NULL,
    term_months INTEGER NOT NULL,
    remaining_term_months INTEGER NOT NULL,
    monthly_payment DECIMAL(19,2) NOT NULL,
    remaining_balance DECIMAL(19,2) NOT NULL,
    total_interest_paid DECIMAL(19,2) DEFAULT 0,
    total_amount_paid DECIMAL(19,2) DEFAULT 0,
    next_payment_date DATE,
    maturity_date DATE NOT NULL,
    disbursement_date DATE,
    user_id UUID NOT NULL,
    account_id UUID,
    purpose TEXT,
    collateral_description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_loans_user_id ON loans(user_id);
CREATE INDEX IF NOT EXISTS idx_loans_account_id ON loans(account_id);
CREATE INDEX IF NOT EXISTS idx_loans_status ON loans(loan_status);
CREATE INDEX IF NOT EXISTS idx_loans_type ON loans(loan_type);
CREATE INDEX IF NOT EXISTS idx_loans_number ON loans(loan_number);
CREATE INDEX IF NOT EXISTS idx_loans_maturity_date ON loans(maturity_date);

-- Add foreign key constraints
ALTER TABLE loans ADD CONSTRAINT fk_loans_user
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

ALTER TABLE loans ADD CONSTRAINT fk_loans_account
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE SET NULL;