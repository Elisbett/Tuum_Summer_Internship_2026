-- Account table
CREATE TABLE IF NOT EXISTS account (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    country VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Balance table (one account can have multiple currencies)
CREATE TABLE IF NOT EXISTS balance (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    currency VARCHAR(3) NOT NULL CHECK (currency IN ('EUR', 'SEK', 'GBP', 'USD')),
    amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(account_id, currency)
);

-- Transaction table
CREATE TABLE IF NOT EXISTS transaction (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL CHECK (currency IN ('EUR', 'SEK', 'GBP', 'USD')),
    direction VARCHAR(3) NOT NULL CHECK (direction IN ('IN', 'OUT')),
    description TEXT NOT NULL,
    balance_after DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for quick searching
CREATE INDEX IF NOT EXISTS idx_balance_account_id ON balance(account_id);
CREATE INDEX IF NOT EXISTS idx_transaction_account_id ON transaction(account_id);
CREATE INDEX IF NOT EXISTS idx_transaction_created_at ON transaction(created_at);