package dev.robocode.tankroyale.botapi.graphics

import dev.robocode.tankroyale.utils.StringFormatUtil

/**
 * A point in a two-dimensional plane.
 */
class Point(
    /** The x-coordinate of this Point. */
    val x: Double,
    /** The y-coordinate of this Point. */
    val y: Double
) {
    /**
     * Returns `true` if [other] is a point with the same coordinates.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Point
        return x == other.x && y == other.y
    }

    /**
     * Hash code based on [x] and [y].
     */
    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    /**
     * Human-readable representation of the point.
     */
    override fun toString(): String {
        return StringFormatUtil.formatPoint(x, y)
    }
}
