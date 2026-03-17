CREATE TABLE otps (
    otp_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    otp_type_id INT NOT NULL,
    otp_code    VARCHAR(10) NOT NULL,
    remaining_attempts   TINYINT DEFAULT 3,
    expired_at  TIMESTAMP NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)     REFERENCES users(user_id)          ON DELETE CASCADE,
    FOREIGN KEY (otp_type_id) REFERENCES otp_types(otp_type_id)  ON DELETE RESTRICT
);