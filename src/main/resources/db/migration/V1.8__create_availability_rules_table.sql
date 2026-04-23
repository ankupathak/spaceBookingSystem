CREATE TABLE availability_rules (
    template_id BIGINT NOT NULL,
    day_of_week ENUM(
            'MONDAY','TUESDAY','WEDNESDAY',
            'THURSDAY','FRIDAY','SATURDAY','SUNDAY'
    ) NOT NULL,
    slots JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES availability_templates(template_id) ON DELETE CASCADE,
    PRIMARY KEY (template_id,day_of_week)
);