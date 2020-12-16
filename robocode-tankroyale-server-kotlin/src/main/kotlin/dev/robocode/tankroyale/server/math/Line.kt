package dev.robocode.tankroyale.server.math

/** Defines a 2D line. */
data class Line(
    /** Start of the line */
    val start: Point,

    /** End of the line */
    val end: Point
) {
    constructor(x1: Double, y1: Double, x2: Double, y2: Double) : this(Point(x1, y1), Point(x2, y2))
}