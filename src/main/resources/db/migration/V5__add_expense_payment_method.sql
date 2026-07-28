ALTER TABLE expense_items
    ADD COLUMN payment_method VARCHAR(10) NOT NULL DEFAULT 'DEBIT';
