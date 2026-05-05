CREATE TYPE booking_status_enum AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED');

CREATE TABLE IF NOT EXISTS bookings (
    booking_id       BIGSERIAL            PRIMARY KEY,
    space_id         BIGINT               NOT NULL,
    booker_user_id   BIGINT               NOT NULL,
    "start"            TIMESTAMP            NOT NULL,
    "end"              TIMESTAMP            NOT NULL,
    booker_timezone  VARCHAR(50)          NOT NULL,
    status           booking_status_enum  NOT NULL DEFAULT 'PENDING',
    buffer_minutes   INT                  NOT NULL DEFAULT 0,
    template_version INT                  NOT NULL,
    rule_version     INT                  NOT NULL,
    created_at       TIMESTAMP            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at     TIMESTAMP            NULL,

    FOREIGN KEY (space_id)       REFERENCES spaces(space_id),
    FOREIGN KEY (booker_user_id) REFERENCES users(user_id)
);

-- Indexes
CREATE INDEX idx_bookings_overlap       ON bookings (space_id, status, start, "end");
CREATE INDEX idx_bookings_user          ON bookings (booker_user_id, status);
CREATE INDEX idx_bookings_space_status  ON bookings (space_id, status);

-- THE reason we switched to PostgreSQL
-- Rejects any INSERT/UPDATE that would create an overlapping CONFIRMED booking
-- No application-level locking needed at all
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE bookings
ADD CONSTRAINT no_overlapping_bookings
EXCLUDE USING GIST (
    space_id                        WITH =,
    tsrange("start", "end", '[)')     WITH &&
) WHERE (status = 'CONFIRMED');