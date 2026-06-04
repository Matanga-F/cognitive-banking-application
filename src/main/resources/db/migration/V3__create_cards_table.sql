-- V3__create_cards_table.sql
-- Baseline migration for the cards table

CREATE TABLE cards (
    card_id UUID PRIMARY KEY,                        -- matches JpaRepository<Card, UUID>
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES accounts(account_id) ON DELETE CASCADE,
    card_number VARCHAR(20) NOT NULL UNIQUE,         -- supports findByCardNumber
    card_type VARCHAR(20) NOT NULL,                  -- maps to CardType enum
    card_status VARCHAR(20) NOT NULL,                -- maps to CardStatus enum
    expiry_date DATE NOT NULL,                       -- supports findExpiredActiveCards
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Helpful indexes for queries in CardRepository
CREATE INDEX idx_cards_user_id ON cards(user_id);
CREATE INDEX idx_cards_account_id ON cards(account_id);
CREATE INDEX idx_cards_card_number ON cards(card_number);
CREATE INDEX idx_cards_status ON cards(card_status);
CREATE INDEX idx_cards_type ON cards(card_type);
CREATE INDEX idx_cards_expiry_date ON cards(expiry_date);
