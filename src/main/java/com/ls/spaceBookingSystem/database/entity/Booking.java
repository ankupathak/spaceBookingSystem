package com.ls.spaceBookingSystem.database.entity;

import com.ls.spaceBookingSystem.common.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "bookings",
        indexes = {
                @Index(name = "idx_bookings_overlap",      columnList = "space_id, status, start, end"),
                @Index(name = "idx_bookings_user",          columnList = "booker_user_id, status"),
                @Index(name = "idx_bookings_space_status",  columnList = "space_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseTimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", insertable = false, updatable = false)
    private Space space;

    @Column(name = "booker_user_id", nullable = false)
    private Long bookerUserId;

    @Column(name = "start", nullable = false)
    private Instant start;

//    @Column(name = "end", nullable = false)
    @Column(name = "\"end\"", nullable = false)
    private Instant end;

    @Column(name = "booker_timezone", nullable = false, length = 50)
    private String bookerTimezone;

    /**
     * @JdbcType(PostgreSQLEnumJdbcType.class) tells Hibernate to use
     * PostgreSQL's native ENUM type (booking_status_enum) instead of
     * storing as VARCHAR. Must match the CREATE TYPE in migration.
     */
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "buffer_minutes",   nullable = false) private int bufferMinutes;
    @Column(name = "template_version", nullable = false) private int templateVersion;
    @Column(name = "rule_version",     nullable = false) private int ruleVersion;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    // ── Domain methods ───────────────────────────────────────────────

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        this.status      = BookingStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    public boolean isActive() {
        return this.status == BookingStatus.CONFIRMED
                || this.status == BookingStatus.PENDING;
    }
}
