-- V5__create_loan_payments_table.sql
-- Baseline migration for the loan payments table

CREATE TABLE loan_payments (
    payment_id UUID PRIMARY KEY,                         -- matches JpaRepository<LoanPayment, UUID>
    loan_id UUID NOT NULL REFERENCES loans(loan_id) ON DELETE CASCADE,
    payment_number INT NOT NULL,                         -- supports ordering and findLastPaymentNumberByLoanId
    due_date DATE NOT NULL,                              -- supports findOverduePayments and findPendingPaymentsByLoanId
    payment_date DATE NULL,                              -- actual payment date
    total_payment NUMERIC(15,2) NOT NULL,                -- supports getTotalPaidAmountByLoanId
    principal_component NUMERIC(15,2) NOT NULL,          -- principal portion
    interest_component NUMERIC(15,2) NOT NULL,           -- interest portion
    payment_status VARCHAR(20) NOT NULL,                 -- e.g., PENDING, PAID, OVERDUE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (loan_id, payment_number)                     -- ensures unique sequence per loan
);

-- Helpful indexes for queries in LoanPaymentRepository
CREATE INDEX idx_loan_payments_loan_id ON loan_payments(loan_id);
CREATE INDEX idx_loan_payments_payment_number ON loan_payments(payment_number);
CREATE INDEX idx_loan_payments_due_date ON loan_payments(due_date);
CREATE INDEX idx_loan_payments_status ON loan_payments(payment_status);
