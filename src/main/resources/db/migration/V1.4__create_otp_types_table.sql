CREATE TABLE otp_types (
    otp_type_id INT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(50) NOT NULL UNIQUE
);