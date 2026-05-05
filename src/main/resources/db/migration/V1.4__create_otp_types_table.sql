CREATE TABLE otp_types (
    otp_type_id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE
);