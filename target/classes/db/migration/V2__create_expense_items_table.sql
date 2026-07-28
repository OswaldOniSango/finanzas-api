CREATE TABLE expense_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_id BIGINT NOT NULL,
    category VARCHAR(100) NOT NULL,
    detail VARCHAR(255),
    amount DECIMAL(18, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL,
    expense_type VARCHAR(20) NOT NULL,
    expense_group VARCHAR(100) NOT NULL,
    note VARCHAR(500),
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_expense_items_period FOREIGN KEY (period_id) REFERENCES financial_periods (id) ON DELETE CASCADE
);

CREATE INDEX idx_expense_items_period ON expense_items (period_id);
