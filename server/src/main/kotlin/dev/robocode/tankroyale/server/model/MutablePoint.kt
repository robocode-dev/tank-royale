package dev.robocode.tankroyale.server.model

/** Defines a mutable 2D point */
data class MutablePoint(
    /** x coordinate */
    override var x: Double,
    /** y coordinate */
    override var y: Double
) : IPoint {
    /** Returns an immutable copy of this point */
    fun toPoint() = Point(x, y)
}
