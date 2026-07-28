CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_users_username UNIQUE (username)
);

ALTER TABLE financial_periods
    ADD COLUMN owner_user_id BIGINT;

ALTER TABLE financial_periods
    ADD CONSTRAINT fk_financial_periods_owner
    FOREIGN KEY (owner_user_id) REFERENCES users (id);
