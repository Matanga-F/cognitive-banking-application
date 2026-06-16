-- V5__create_loan_payments_table.sql
-- Create loan payments table

CREATE TABLE IF NOT EXISTS loan_payments (
    payment_id UUID PRIMARY KEY,
    loan_id UUID NOT NULL,
    payment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    payment_date DATE,
    principal_amount DECIMAL(19,2) NOT NULL,
    interest_amount DECIMAL(19,2) NOT NULL,
    total_payment DECIMAL(19,2) NOT NULL,
    remaining_balance DECIMAL(19,2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    paid_date DATE,
    late_fee DECIMAL(19,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_loan_payments_loan_id ON loan_payments(loan_id);
CREATE INDEX IF NOT EXISTS idx_loan_payments_status ON loan_payments(payment_status);
CREATE INDEX IF NOT EXISTS idx_loan_payments_due_date ON loan_payments(due_date);
CREATE INDEX IF NOT EXISTS idx_loan_payments_loan_number ON loan_payments(loan_id, payment_number);

-- Add foreign key constraint
ALTER TABLE loan_payments ADD CONSTRAINT fk_loan_payments_loan
    FOREIGN KEY (loan_id) REFERENCES loans(loan_id) ON DELETE CASCADE;