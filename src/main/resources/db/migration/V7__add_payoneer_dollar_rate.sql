ALTER TABLE financial_periods
    ADD COLUMN payoneer_dollar_rate DECIMAL(18, 4) NOT NULL DEFAULT 1;

UPDATE financial_periods
SET payoneer_dollar_rate = reference_rate;
