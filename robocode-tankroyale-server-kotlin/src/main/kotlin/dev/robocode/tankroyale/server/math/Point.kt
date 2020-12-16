package dev.robocode.tankroyale.server.math

/** Defines an immutable 2D point */
data class Point(
    /** x coordinate */
    override val x: Double,
    /** y coordinate */
    override val y: Double
): IPoint {
    /** Returns a mutable version of this point */
    fun toMutablePoint() = MutablePoint(x, y)
}