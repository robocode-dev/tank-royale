package dev.robocode.tankroyale.botapi.graphics

/**
 * Graphics context capable of drawing primitives.  
 * Shared across the WASM target and keeps parity with the original Java API while using Kotlin
 * collection types.
 */
interface IGraphics {

    /** Draws a line from point (x1, y1) to point (x2, y2). */
    fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double)

    /** Draws the outline of a rectangle. */
    fun drawRectangle(x: Double, y: Double, width: Double, height: Double)

    /** Fills a rectangle with the current fill color. */
    fun fillRectangle(x: Double, y: Double, width: Double, height: Double)

    /** Draws the outline of a circle. */
    fun drawCircle(x: Double, y: Double, radius: Double)

    /** Fills a circle with the current fill color. */
    fun fillCircle(x: Double, y: Double, radius: Double)

    /** Draws the outline of a polygon defined by a list of points. */
    fun drawPolygon(points: List<Point>)

    /** Fills a polygon defined by a list of points with the current fill color. */
    fun fillPolygon(points: List<Point>)

    /** Draws text at the specified position. */
    fun drawText(text: String, x: Double, y: Double)

    /** Sets the color used for drawing outlines. */
    fun setStrokeColor(color: Color)

    /** Sets the color used for filling shapes. */
    fun setFillColor(color: Color)

    /** Sets the width of the stroke used for drawing outlines. */
    fun setStrokeWidth(width: Double)

    /** Sets the font used for drawing text. */
    fun setFont(fontFamily: String, fontSize: Double)

    /** Generates the SVG representation of all drawing operations. */
    fun toSvg(): String

    /** Clears all drawing operations. */
    fun clear()
}
