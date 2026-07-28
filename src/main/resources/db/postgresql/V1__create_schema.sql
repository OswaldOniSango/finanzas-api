CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE financial_periods (
    id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    period_year INT NOT NULL,
    period_month INT NOT NULL,
    salary_ars DECIMAL(18, 2) NOT NULL DEFAULT 0,
    salary_usd DECIMAL(18, 2) NOT NULL DEFAULT 0,
    reference_rate DECIMAL(18, 4) NOT NULL DEFAULT 1,
    card_dollar_rate DECIMAL(18, 4) NOT NULL DEFAULT 1,
    payoneer_dollar_rate DECIMAL(18, 4) NOT NULL DEFAULT 1,
    conservative_base_usd DECIMAL(18, 2) NOT NULL DEFAULT 0,
    apartment_target_price_usd DECIMAL(18, 2) NOT NULL DEFAULT 0,
    apartment_down_payment_percent DECIMAL(9, 6) NOT NULL DEFAULT 0,
    apartment_current_savings_usd DECIMAL(18, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_financial_periods_owner FOREIGN KEY (owner_user_id) REFERENCES users (id),
    CONSTRAINT uk_financial_periods_owner_year_month UNIQUE (owner_user_id, period_year, period_month)
);

CREATE TABLE expense_items (
    id BIGSERIAL PRIMARY KEY,
    period_id BIGINT NOT NULL,
    category VARCHAR(100) NOT NULL,
    detail VARCHAR(255),
    amount DECIMAL(18, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL,
    expense_type VARCHAR(20) NOT NULL,
    payment_method VARCHAR(10) NOT NULL DEFAULT 'DEBIT',
    expense_group VARCHAR(100) NOT NULL,
    note VARCHAR(500),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_expense_items_period FOREIGN KEY (period_id) REFERENCES financial_periods (id) ON DELETE CASCADE
);

CREATE INDEX idx_expense_items_period ON expense_items (period_id);

CREATE TABLE credit_cards (
    id BIGSERIAL PRIMARY KEY,
    period_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    balance DECIMAL(18, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL,
    minimum_payment DECIMAL(18, 2) NOT NULL DEFAULT 0,
    annual_rate_percent DECIMAL(9, 4) NOT NULL DEFAULT 0,
    due_date DATE,
    monthly_payment DECIMAL(18, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_credit_cards_period FOREIGN KEY (period_id) REFERENCES financial_periods (id) ON DELETE CASCADE
);

CREATE INDEX idx_credit_cards_period ON credit_cards (period_id);

CREATE TABLE plan_allocations (
    id BIGSERIAL PRIMARY KEY,
    period_id BIGINT NOT NULL,
    stage VARCHAR(30) NOT NULL,
    concept VARCHAR(120) NOT NULL,
    percentage DECIMAL(9, 6) NOT NULL DEFAULT 0,
    objective VARCHAR(500),
    allocation_role VARCHAR(30) NOT NULL DEFAULT 'NONE',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_plan_allocations_period FOREIGN KEY (period_id) REFERENCES financial_periods (id) ON DELETE CASCADE
);

CREATE INDEX idx_plan_allocations_period ON plan_allocations (period_id);
