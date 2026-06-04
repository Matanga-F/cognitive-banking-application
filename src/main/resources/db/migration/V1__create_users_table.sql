-- V1__create_users_table.sql
-- Baseline migration for the users table

CREATE TABLE users (
    user_id UUID PRIMARY KEY,                  -- matches JpaRepository<User, UUID>
    username VARCHAR(50) NOT NULL UNIQUE,      -- login/display name
    email VARCHAR(100) NOT NULL UNIQUE,        -- used for authentication
    phone_number VARCHAR(20) UNIQUE,           -- optional, used for auth/lookup
    password VARCHAR(255) NOT NULL,            -- hashed password
    role VARCHAR(20) NOT NULL,                 -- maps to UserRole enum
    status VARCHAR(20) NOT NULL,               -- maps to UserStatus enum

    locked BOOLEAN NOT NULL DEFAULT false,     -- account lock flag
    locked_until TIMESTAMP NULL,               -- temporary lock expiry

    password_expiry_date TIMESTAMP NULL,       -- for password rotation policies
    account_expiry_date TIMESTAMP NULL,        -- dormancy/expiry tracking

    last_login_at TIMESTAMP NULL,              -- last login timestamp
    mfa_secret VARCHAR(255) NULL,              -- TOTP secret for MFA

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Helpful indexes for queries in UserRepository
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone_number ON users(phone_number);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_locked_until ON users(locked_until);
