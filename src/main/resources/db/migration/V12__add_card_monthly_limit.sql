ALTER TABLE financial_periods
    ADD COLUMN card_monthly_limit_usd DECIMAL(18, 2) NOT NULL DEFAULT 0;
