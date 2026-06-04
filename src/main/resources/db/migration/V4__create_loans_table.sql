-- V4__create_loans_table.sql
-- Baseline migration for the loans table

CREATE TABLE loans (
    loan_id UUID PRIMARY KEY,                          -- matches JpaRepository<Loan, UUID>
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES accounts(account_id) ON DELETE CASCADE,
    loan_number VARCHAR(30) NOT NULL UNIQUE,           -- supports findByLoanNumber
    loan_type VARCHAR(20) NOT NULL,                    -- maps to LoanType enum
    loan_status VARCHAR(20) NOT NULL,                  -- maps to LoanStatus enum
    principal_amount NUMERIC(15,2) NOT NULL,           -- original loan amount
    interest_rate NUMERIC(5,2) NOT NULL,               -- interest rate %
    remaining_balance NUMERIC(15,2) NOT NULL,          -- supports getTotalOutstandingBalanceByUserId
    start_date DATE NOT NULL,                          -- loan start date
    maturity_date DATE NOT NULL,                       -- supports findMaturedLoans
    next_payment_date DATE NULL,                       -- supports findLoansWithDuePayments
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Helpful indexes for queries in LoanRepository
CREATE INDEX idx_loans_user_id ON loans(user_id);
CREATE INDEX idx_loans_account_id ON loans(account_id);
CREATE INDEX idx_loans_number ON loans(loan_number);
CREATE INDEX idx_loans_status ON loans(loan_status);
CREATE INDEX idx_loans_type ON loans(loan_type);
CREATE INDEX idx_loans_next_payment_date ON loans(next_payment_date);
CREATE INDEX idx_loans_maturity_date ON loans(maturity_date);
