package dev.robocode.tankroyale.server.model

/** Defines an immutable 2D point */
data class Point(
    /** x coordinate */
    override val x: Double,
    /** y coordinate */
    override val y: Double
): IPoint {
    /** Returns a mutable copy of this point */
    fun toMutablePoint() = MutablePoint(x, y)
}