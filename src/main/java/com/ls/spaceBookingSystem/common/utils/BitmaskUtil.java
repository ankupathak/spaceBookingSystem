package com.ls.spaceBookingSystem.common.utils;

import java.math.BigInteger;
import java.time.LocalTime;

public class BitmaskUtil {

    public static final int TOTAL_MINUTES = 1440;   // minutes in a day
    public static final int MASK_BYTES    = 180;    // 1440 / 8

    private BitmaskUtil() {}

    /**
     * Converts a LocalTime to its minute-of-day index [0, 1439].
     * Seconds and nanoseconds are ignored — callers must truncate beforehand.
     */
    public static int toMinuteOfDay(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    /**
     * Generates a 180-byte bitmask covering [startMin, endMin).
     *
     * @param startMin inclusive start, in [0, 1440)
     * @param endMin   exclusive end,   in (0, 1440]
     * @throws IllegalArgumentException for out-of-range or zero-length inputs
     */
    public static byte[] generateMask(int startMin, int endMin) {
        validateRange(startMin, endMin);

        int length = endMin - startMin;

        // Build `length` consecutive 1-bits then shift them to position startMin.
        // Example: length=3, startMin=5 → 0b...0011100000
        BigInteger ones          = BigInteger.ONE.shiftLeft(length).subtract(BigInteger.ONE);
        BigInteger positionedMask = ones.shiftLeft(startMin);

        return toFixedByteArray(positionedMask);
    }

    // -------------------------------------------------------------------------

    private static void validateRange(int startMin, int endMin) {
        if (startMin < 0 || startMin >= TOTAL_MINUTES)
            throw new IllegalArgumentException(
                    "startMin out of range [0, 1440): " + startMin);
        if (endMin <= 0 || endMin > TOTAL_MINUTES)
            throw new IllegalArgumentException(
                    "endMin out of range (0, 1440]: " + endMin);
        if (startMin >= endMin)
            throw new IllegalArgumentException(
                    "startMin must be < endMin, got: " + startMin + " >= " + endMin);
    }

    private static byte[] toFixedByteArray(BigInteger val) {
        byte[] raw    = val.toByteArray();
        byte[] padded = new byte[MASK_BYTES];

        // toByteArray() prepends a 0x00 sign byte for positive values — skip it.
        // Then right-align the magnitude bytes inside our fixed 180-byte array.
        int bytesToCopy = Math.min(raw.length, MASK_BYTES);
        int srcPos      = raw.length - bytesToCopy;   // skips sign byte when present
        System.arraycopy(raw, srcPos, padded, MASK_BYTES - bytesToCopy, bytesToCopy);
        return padded;
    }
}
