package dev.robocode.tankroyale.botapi.graphics

import dev.robocode.tankroyale.utils.StringFormatUtil

/**
 * Represents an ordered pair of x and y coordinates that define a point in a two-dimensional plane.
 */
class Point(
    /** The x-coordinate of this Point. */
    val x: Double,
    /** The y-coordinate of this Point. */
    val y: Double
) {

    /**
     * Determines whether the specified object is equal to the current Point.
     *
     * @param other The object to compare with the current Point.
     * @return true if the specified object is equal to the current Point; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Point
        return x == other.x && y == other.y
    }

    /**
     * Returns the hash code for this Point.
     *
     * @return A hash code for the current Point.
     */
    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    /**
     * Returns a string that represents the current Point.
     *
     * @return A string that represents the current Point.
     */
    override fun toString(): String {
        return StringFormatUtil.formatPoint(x, y)
    }
}
