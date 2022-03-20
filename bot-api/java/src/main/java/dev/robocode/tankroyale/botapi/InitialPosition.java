package dev.robocode.tankroyale.botapi;

/**
 * Initial starting position containing a start coordinate (x,y) and angle.
 * <p>
 * The initial position is only used when debugging to request the server to let a bot start at a specific position.
 * Note that initial starting positions must be enabled at the server-side; otherwise the initial starting position
 * is ignored.
 */
public final class InitialPosition {

    private final Double x, y, angle;

    /**
     * Initializes a new instance of the InitialPosition class.
     *
     * @param x     is the x coordinate, where {@code null} means it is random.
     * @param y     is the y coordinate, where {@code null} means it is random.
     * @param angle is the angle, where {@code null} means it is random.
     */
    private InitialPosition(Double x, Double y, Double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
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
     * Returns the angle;
     *
     * @return The angle or {@code null} if no angle is specified and a random value must be used.
     */
    public Double getAngle() {
        return angle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (x == null && y == null && angle == null) return "";
        var x = this.x == null ? "" : this.x;
        var y = this.y == null ? "" : this.y;
        var angle = this.angle == null ? "" : this.angle;
        return x + "," + y + "," + angle;
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
        Double angle = null;
        if (values.length >= 3) {
            angle = parseDouble(values[2]);
        }
        return new InitialPosition(x, y, angle);
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