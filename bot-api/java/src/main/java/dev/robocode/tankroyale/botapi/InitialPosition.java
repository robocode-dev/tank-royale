package dev.robocode.tankroyale.botapi;

import java.util.Objects;

/**
 * Initial starting position containing a start coordinate (x,y) and the shared direction of the body, gun, and radar.
 * <p>
 * The initial position is only used when debugging to request the server to let a bot start at a specific position.
 * Note that initial starting positions must be enabled at the server-side; otherwise the initial starting position
 * is ignored.
 */
public final class InitialPosition {

    private final Double x;
    private final Double y;
    private final Double direction;

    /**
     * Initializes a new instance of the InitialPosition class.
     *
     * @param x         is the x coordinate, where {@code null} means it is random.
     * @param y         is the y coordinate, where {@code null} means it is random.
     * @param direction is the shared direction of the body, gun, and radar, where {@code null} means it is random.
     */
    public InitialPosition(Double x, Double y, Double direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InitialPosition that = (InitialPosition) o;
        return Objects.equals(x, that.x) && Objects.equals(y, that.y) && Objects.equals(direction, that.direction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y, direction);
    }

    /**
     * Returns the x coordinate;
     *
     * @return The x coordinate or {@code null} if no x coordinate is specified and a random value must be used.
     */
    public Double getX() {
        return x;
    }

    /**
     * Returns the y coordinate;
     *
     * @return The y coordinate or {@code null} if no y coordinate is specified and a random value must be used.
     */
    public Double getY() {
        return y;
    }

    /**
     * Returns the shared direction of the body, gun, and radar;
     *
     * @return The direction or {@code null} if no direction is specified and a random value must be used.
     */
    public Double getDirection() {
        return direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (x == null && y == null && direction == null) return "";
        var strX = x == null ? "" : x;
        var strY = y == null ? "" : y;
        var strDirection = direction == null ? "" : direction;
        return strX + "," + strY + "," + strDirection;
    }

    /**
     * Creates new instance of the InitialPosition class from a string.
     *
     * @param initialPosition is comma and/or white-space separated string.
     * @return An InitialPosition instance.
     */
    public static InitialPosition fromString(String initialPosition) {
        if (initialPosition == null || initialPosition.isBlank()) return null;
        var values = initialPosition.trim().split("\\s*,\\s*|\\s+");
        return parseInitialPosition(values);
    }

    private static InitialPosition parseInitialPosition(String[] values) {
        if (values.length < 1) return null;

        var x = parseDouble(values[0]);
        if (values.length < 2) {
            return new InitialPosition(x, null, null);
        }
        var y = parseDouble(values[1]);
        Double direction = null;
        if (values.length >= 3) {
            direction = parseDouble(values[2]);
        }
        return new InitialPosition(x, y, direction);
    }

    private static Double parseDouble(String str) {
        if (str == null) return null;
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}