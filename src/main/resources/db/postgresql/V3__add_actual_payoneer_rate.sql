ALTER TABLE monthly_actuals
    ADD COLUMN actual_payoneer_rate DECIMAL(18, 4) NOT NULL DEFAULT 0;
