-- V3__create_cards_table.sql
-- Create cards table

CREATE TABLE IF NOT EXISTS cards (
    card_id UUID PRIMARY KEY,

    user_id UUID NOT NULL,
    account_id UUID NOT NULL,

    card_holder_name VARCHAR(100) NOT NULL,
    card_number VARCHAR(20) NOT NULL UNIQUE,

    cvv VARCHAR(255) NOT NULL,
    pin VARCHAR(255) NOT NULL,

    card_type VARCHAR(20) NOT NULL,
    card_network VARCHAR(20) NOT NULL,

    card_status VARCHAR(20) NOT NULL DEFAULT 'PENDING_ACTIVATION',

    credit_limit DECIMAL(19,2),

    daily_limit DECIMAL(19,2) NOT NULL DEFAULT 1000.00,
    weekly_limit DECIMAL(19,2),
    monthly_limit DECIMAL(19,2),

    available_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    current_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,

    expiry_date DATE NOT NULL,
    issued_date DATE NOT NULL DEFAULT CURRENT_DATE,

    activated_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cards_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_cards_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(account_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cards_user_id
    ON cards(user_id);

CREATE INDEX IF NOT EXISTS idx_cards_account_id
    ON cards(account_id);

CREATE INDEX IF NOT EXISTS idx_cards_card_number
    ON cards(card_number);

CREATE INDEX IF NOT EXISTS idx_cards_status
    ON cards(card_status);

CREATE INDEX IF NOT EXISTS idx_cards_type
    ON cards(card_type);

CREATE INDEX IF NOT EXISTS idx_cards_network
    ON cards(card_network);

CREATE INDEX IF NOT EXISTS idx_cards_expiry_date
    ON cards(expiry_date);

CREATE INDEX IF NOT EXISTS idx_cards_card_holder_name
    ON cards(card_holder_name);

CREATE INDEX IF NOT EXISTS idx_cards_user_status
    ON cards(user_id, card_status);

CREATE INDEX IF NOT EXISTS idx_cards_account_status
    ON cards(account_id, card_status);