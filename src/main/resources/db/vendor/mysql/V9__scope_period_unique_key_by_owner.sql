ALTER TABLE financial_periods
    DROP INDEX uk_financial_periods_year_month,
    ADD CONSTRAINT uk_financial_periods_owner_year_month
    UNIQUE (owner_user_id, period_year, period_month);
