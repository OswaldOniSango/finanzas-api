CREATE TABLE financial_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_year INT NOT NULL,
    period_month INT NOT NULL,
    salary_ars DECIMAL(18, 2) NOT NULL DEFAULT 0,
    salary_usd DECIMAL(18, 2) NOT NULL DEFAULT 0,
    reference_rate DECIMAL(18, 4) NOT NULL DEFAULT 1,
    conservative_base_usd DECIMAL(18, 2) NOT NULL DEFAULT 0,
    apartment_target_price_usd DECIMAL(18, 2) NOT NULL DEFAULT 0,
    apartment_down_payment_percent DECIMAL(9, 6) NOT NULL DEFAULT 0,
    apartment_current_savings_usd DECIMAL(18, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(1000),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_financial_periods_year_month UNIQUE (period_year, period_month)
);
