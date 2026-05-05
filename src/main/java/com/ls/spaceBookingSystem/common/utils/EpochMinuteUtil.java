package com.ls.spaceBookingSystem.common.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class EpochMinuteUtil {

    private EpochMinuteUtil() {}

    /**
     * Convert a LocalDateTime (UTC) to minutes since Unix epoch.
     * e.g. 2025-02-04T10:00:00 UTC → some large integer
     */
    public static long toEpochMinute(LocalDateTime dt) {
        return dt.toEpochSecond(ZoneOffset.UTC) / 60;
    }

    /**
     * Convert epoch minutes back to LocalDateTime (UTC).
     */
    public static LocalDateTime fromEpochMinute(long epochMinutes) {
        return LocalDateTime.ofEpochSecond(epochMinutes * 60, 0, ZoneOffset.UTC);
    }

    /**
     * Snap a LocalDateTime DOWN to the nearest minute boundary.
     * e.g. 10:07:45 → 10:07:00
     * We use minute precision throughout — seconds are stripped.
     */
    public static LocalDateTime snapToMinute(LocalDateTime dt) {
        return dt.withSecond(0).withNano(0);
    }
}
