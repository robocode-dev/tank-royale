package dev.robocode.tankroyale.botapi.internal;

public final class MathUtil {

    public static double limitRange(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }
}