CREATE TABLE availability_templates (
    template_id         BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    name                VARCHAR(30),
    min_booking_minutes INT          NOT NULL,
    max_booking_minutes INT          NOT NULL,
    buffer_minutes      INT          NOT NULL DEFAULT 0,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    template_version    INT          NOT NULL DEFAULT 1,
    rule_version        INT          NOT NULL DEFAULT 1,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id)
);