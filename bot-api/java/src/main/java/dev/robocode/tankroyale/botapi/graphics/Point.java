package dev.robocode.tankroyale.botapi.graphics;

import java.util.Objects;

/**
 * Represents an ordered pair of x and y coordinates that define a point in a two-dimensional plane.
 */
public class Point {

    /** The x-coordinate of this Point. */
    private final double x;

    /** The y-coordinate of this Point. */
    private final double y;

    /**
     * Initializes a new instance of the Point class with the specified coordinates.
     *
     * @param x The x-coordinate of the point.
     * @param y The y-coordinate of the point.
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x-coordinate of this Point.
     *
     * @return The x-coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of this Point.
     *
     * @return The y-coordinate.
     */
    public double getY() {
        return y;
    }

    /**
     * Determines whether the specified object is equal to the current Point.
     *
     * @param obj The object to compare with the current Point.
     * @return true if the specified object is equal to the current Point; otherwise, false.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Point other = (Point) obj;
        final double epsilon = 1e-10; // Define a tolerance for floating-point comparisons
        return Math.abs(x - other.x) < epsilon && Math.abs(y - other.y) < epsilon;
    }

    /**
     * Returns the hash code for this Point.
     *
     * @return A hash code for the current Point.
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    /**
     * Returns a string that represents the current Point.
     *
     * @return A string that represents the current Point.
     */
    @Override
    public String toString() {
        return String.format("(X=%s, Y=%s)", Double.toString(x), Double.toString(y));
    }
}
