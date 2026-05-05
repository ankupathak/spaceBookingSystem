CREATE TABLE users (
    user_id            BIGSERIAL PRIMARY KEY,
    full_name          VARCHAR(255) NOT NULL,
    email              VARCHAR(25)  NOT NULL UNIQUE,
    is_email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    password_hash      VARCHAR(60),
    tokens_valid_after TIMESTAMP    NOT NULL,
    timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata',
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);