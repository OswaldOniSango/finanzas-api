CREATE TABLE monthly_actuals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_id BIGINT NOT NULL,
    usd_exchanged DECIMAL(18, 2) NOT NULL DEFAULT 0,
    ars_received DECIMAL(18, 2) NOT NULL DEFAULT 0,
    card_payments_ars DECIMAL(18, 2) NOT NULL DEFAULT 0,
    card_payments_usd DECIMAL(18, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(1000),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_monthly_actuals_period FOREIGN KEY (period_id) REFERENCES financial_periods (id) ON DELETE CASCADE,
    CONSTRAINT uk_monthly_actuals_period UNIQUE (period_id)
);
