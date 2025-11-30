-- scripts/init-database.sql
-- Enable UUID extension for PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create indexes for better performance (if they don't exist)
DO $$
BEGIN
    -- Users table indexes
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_users_email') THEN
        CREATE INDEX idx_users_email ON users(email);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_users_phone') THEN
        CREATE INDEX idx_users_phone ON users(phone_number);
    END IF;

    -- Accounts table indexes
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_accounts_user_id') THEN
        CREATE INDEX idx_accounts_user_id ON accounts(user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_accounts_number') THEN
        CREATE INDEX idx_accounts_number ON accounts(account_number);
    END IF;

    -- Cards table indexes
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_cards_user_id') THEN
        CREATE INDEX idx_cards_user_id ON cards(user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_cards_account_id') THEN
        CREATE INDEX idx_cards_account_id ON cards(account_id);
    END IF;

    -- Transactions table indexes
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_transactions_account_id') THEN
        CREATE INDEX idx_transactions_account_id ON transactions(from_account_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_transactions_date') THEN
        CREATE INDEX idx_transactions_date ON transactions(transaction_date);
    END IF;

    -- Loans table indexes
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_loans_user_id') THEN
        CREATE INDEX idx_loans_user_id ON loans(user_id);
    END IF;

    -- Loan Payments table indexes
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_loan_payments_loan_id') THEN
        CREATE INDEX idx_loan_payments_loan_id ON loan_payments(loan_id);
    END IF;

    RAISE NOTICE 'Database indexes created/verified successfully';
EXCEPTION
    WHEN others THEN
        RAISE NOTICE 'Error creating indexes: %', SQLERRM;
END $$;