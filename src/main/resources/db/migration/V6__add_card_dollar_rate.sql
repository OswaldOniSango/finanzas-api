ALTER TABLE financial_periods
    ADD COLUMN card_dollar_rate DECIMAL(18, 4) NOT NULL DEFAULT 1;

UPDATE financial_periods
SET card_dollar_rate = reference_rate;
