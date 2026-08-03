ALTER TABLE expense_items
    ADD COLUMN counts_toward_card_limit BOOLEAN NOT NULL DEFAULT TRUE;
