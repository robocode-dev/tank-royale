package dev.robocode.tankroyale.botapi.util;

/**
 * Math utility class.
 */
public final class MathUtil {

    // Hides constructor
    private MathUtil() {
    }

    /**
     * Returns value clamped to the inclusive range of min and max.
     *
     * @param value is the value to be clamped.
     * @param min   is the lower bound of the result.
     * @param max   is the upper bound of the result.
     * @return is the clamped value.
     */
    public static double clamp(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }
}
