-- V1__create_users_table.sql (Simplified - no partial indexes)
CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    locked BOOLEAN NOT NULL DEFAULT false,
    locked_until TIMESTAMP,
    password_expiry_date TIMESTAMP,
    account_expiry_date TIMESTAMP,
    mfa_secret VARCHAR(64),
    email_verification_token VARCHAR(255),
    email_verification_token_expiry TIMESTAMP,
    email_verified BOOLEAN DEFAULT false,
    two_factor_secret VARCHAR(255),
    two_factor_enabled BOOLEAN DEFAULT false,
    phone_verified BOOLEAN DEFAULT false,
    reset_token VARCHAR(255),
    reset_token_expiry TIMESTAMP,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create all indexes (non-partial)
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone_number ON users(phone_number);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_locked_until ON users(locked_until);
CREATE INDEX IF NOT EXISTS idx_users_reset_token ON users(reset_token);
CREATE INDEX IF NOT EXISTS idx_users_email_verification_token ON users(email_verification_token);
CREATE INDEX IF NOT EXISTS idx_users_two_factor_enabled ON users(two_factor_enabled);