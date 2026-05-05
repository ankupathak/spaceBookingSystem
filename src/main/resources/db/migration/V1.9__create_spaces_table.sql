CREATE TABLE spaces (
    space_id    BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    template_id BIGINT       NOT NULL,
    name        VARCHAR(30)  NOT NULL,
    description VARCHAR(255) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (template_id) REFERENCES availability_templates(template_id),
    FOREIGN KEY (user_id)     REFERENCES users(user_id)
);