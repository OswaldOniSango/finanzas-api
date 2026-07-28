CREATE TABLE credit_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_credit_cards_period FOREIGN KEY (period_id) REFERENCES financial_periods (id) ON DELETE CASCADE
);

CREATE INDEX idx_credit_cards_period ON credit_cards (period_id);
