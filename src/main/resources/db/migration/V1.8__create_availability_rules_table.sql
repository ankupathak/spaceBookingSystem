CREATE TYPE day_of_week_enum AS ENUM (
    'MONDAY','TUESDAY','WEDNESDAY',
    'THURSDAY','FRIDAY','SATURDAY','SUNDAY'
);

CREATE TABLE availability_rules (
    template_id BIGINT           NOT NULL,
    day_of_week day_of_week_enum NOT NULL,
    is_full_day BOOLEAN          NOT NULL DEFAULT FALSE,
    slots       JSONB            NOT NULL DEFAULT '[]',
    created_at  TIMESTAMP                 DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP                 DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (template_id, day_of_week),
    FOREIGN KEY (template_id) REFERENCES availability_templates(template_id) ON DELETE CASCADE
);