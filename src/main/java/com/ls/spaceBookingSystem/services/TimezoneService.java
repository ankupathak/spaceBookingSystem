package com.ls.spaceBookingSystem.services;

import com.ls.spaceBookingSystem.common.errors.ErrorCode;
import com.ls.spaceBookingSystem.common.exceptions.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TimezoneService {
    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    // ── Validation ────────────────────────────────────────────────────────────

    /**
     * Validates and parses an IANA timezone string.
     * Throws InvalidValueException (surfaces as 400) if the zoneId is invalid.
     *
     * @param zoneId  e.g. "Asia/Kolkata", "Europe/London", "UTC"
     */
    public ZoneId parseAndValidate(String zoneId) {
        if (zoneId == null || zoneId.isBlank()) {
            throw new AppException(ErrorCode.UNEXPECTED).withDevMessage("Timezone is required");
        }
        try {
            return ZoneId.of(zoneId);
        } catch (DateTimeException e) {
            throw new AppException(ErrorCode.UNEXPECTED)
                    .withDevMessage("Invalid timezone: '" + zoneId + "'. Must be a valid IANA timezone " +
                            "(e.g. Asia/Kolkata, Europe/London, UTC)");
        }
    }

    /**
     * Returns true if the given string is a valid IANA timezone identifier.
     * Use this for soft checks — use parseAndValidate() when you need to throw.
     */
    public boolean isValid(String zoneId) {
        if (zoneId == null || zoneId.isBlank()) return false;
        try {
            ZoneId.of(zoneId);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }

    /**
     * Strips seconds and nanoseconds from a LocalDateTime, keeping only up to minutes.
     *
     * Must be called BEFORE converting LocalDateTime → Instant.
     * This ensures epoch minute calculations are consistent —
     * two bookings starting at 10:00:30 and 10:00:55 both become 10:00,
     * producing the same epochStart value for overlap detection.
     *
     * Example:
     *   2025-02-04T10:07:45.123456789 → 2025-02-04T10:07:00
     */
    public LocalDateTime truncateToMinute(LocalDateTime dt) {
        return dt.truncatedTo(ChronoUnit.MINUTES);
    }

    // ── Conversion: user local → Instant (for storage) ────────────────────────

    /**
     * Converts a user-provided LocalDateTime to a UTC Instant using their timezone.
     *
     * Example:
     *   toInstant(LocalDateTime.of(2025,6,10,15,30), "Asia/Kolkata")
     *   → 2025-06-10T10:00:00Z  (IST is UTC+5:30)
     */
    public Instant toInstant(LocalDateTime localDateTime, String userTimezone) {
        return toInstant(localDateTime, parseAndValidate(userTimezone));
    }

    public Instant toInstant(LocalDateTime localDateTime, ZoneId userZone) {
        if (localDateTime == null) {
            throw new AppException(ErrorCode.UNEXPECTED)
                    .withDevMessage("DateTime must not be null");
        }
        return localDateTime.atZone(userZone).toInstant();
    }

    // ── Conversion: Instant → user local (for display) ────────────────────────

    /**
     * Converts a stored UTC Instant back to the user's local time.
     * Always use the timezone snapshotted on the booking — not the user's
     * current setting — so historical bookings display correctly.
     *
     * Example:
     *   toUserTime(2025-06-10T10:00:00Z, "Asia/Kolkata")
     *   → 2025-06-10T15:30:00+05:30
     */
    public ZonedDateTime toUserTime(Instant instant, String userTimezone) {
        return toUserTime(instant, parseAndValidate(userTimezone));
    }

    public ZonedDateTime toUserTime(Instant instant, ZoneId userZone) {
        if (instant == null) {
            throw new AppException(ErrorCode.UNEXPECTED)
                    .withDevMessage("Instant must not be null");
        }
        return instant.atZone(userZone);
    }

    /**
     * Convenience: returns the display string for a stored Instant
     * formatted in the user's timezone.
     *
     * Example output: "2025-06-10 15:30:00 IST"
     */
    public String toDisplayString(Instant instant, String userTimezone) {
        return toUserTime(instant, userTimezone).format(DISPLAY_FMT);
    }

    // ── Current time helpers ──────────────────────────────────────────────────

    /**
     * Returns the current moment as an Instant (UTC).
     * Centralised here so tests can subclass and override if needed.
     */
    public Instant now() {
        return Instant.now();
    }

    /**
     * Returns the current time in the user's timezone.
     */
    public ZonedDateTime nowInZone(String userTimezone) {
        return now().atZone(parseAndValidate(userTimezone));
    }

    // ── Epoch minutes (for booking conflict detection) ────────────────────────

    /**
     * Converts an Instant to epoch minutes (floor).
     * Used to populate epoch_start / epoch_end on the Booking entity.
     *
     * epoch_minutes = floor(epochSeconds / 60)
     */
    public long toEpochMinutes(Instant instant) {
        return instant.getEpochSecond() / 60L;
    }

    // ── Available timezones ───────────────────────────────────────────────────

    /**
     * Returns all valid IANA timezone IDs sorted alphabetically.
     * Useful for populating a timezone picker in the frontend.
     */
    public List<String> availableTimezones() {
        return ZoneId.getAvailableZoneIds()
                .stream()
                .sorted()
                .toList();
    }

    /**
     * Returns the default timezone for this application (Asia/Kolkata).
     */
    public ZoneId defaultZone() {
        return DEFAULT_ZONE;
    }
}
