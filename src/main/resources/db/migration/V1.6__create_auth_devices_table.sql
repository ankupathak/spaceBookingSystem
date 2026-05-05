CREATE TABLE auth_devices (
    device_id   VARCHAR(36)  NOT NULL,
    user_id     BIGINT       NOT NULL,
    device_info VARCHAR(255),
    expires_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, device_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);