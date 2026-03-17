CREATE TABLE users (
    user_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name         VARCHAR(255) NOT NULL,
    email             VARCHAR(25) NOT NULL UNIQUE,
    is_email_verified BOOLEAN DEFAULT FALSE,
    password_hash     VARCHAR(60),
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);