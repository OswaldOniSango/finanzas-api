CREATE TABLE plan_allocations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_id BIGINT NOT NULL,
    stage VARCHAR(30) NOT NULL,
    concept VARCHAR(120) NOT NULL,
    percentage DECIMAL(9, 6) NOT NULL DEFAULT 0,
    objective VARCHAR(500),
    allocation_role VARCHAR(30) NOT NULL DEFAULT 'NONE',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_plan_allocations_period FOREIGN KEY (period_id) REFERENCES financial_periods (id) ON DELETE CASCADE
);

CREATE INDEX idx_plan_allocations_period ON plan_allocations (period_id);
