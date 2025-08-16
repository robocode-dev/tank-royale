package dev.robocode.tankroyale.botapi.graphics

import dev.robocode.tankroyale.utils.NumberFormatUtil.format

/**
 * [IGraphics] implementation that produces SVG markup.
 */
class SvgGraphics : IGraphics {

    private val elements = mutableListOf<String>()
    private var strokeColor: String = "none"
    private var fillColor: String = "none"
    private var strokeWidth: Double = 0.0
    private var fontFamily: String = "Arial"
    private var fontSize: Double = 12.0

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        elements += "<line " +
                "x1=\"${format(x1)}\" " +
                "y1=\"${format(y1)}\" " +
                "x2=\"${format(x2)}\" " +
                "y2=\"${format(y2)}\" " +
                "stroke=\"$strokeColor\" " +
                "stroke-width=\"${format(strokeWidth)}\" " +
                "/>\n"
    }

    override fun drawRectangle(x: Double, y: Double, width: Double, height: Double) {
        val sColor = if (strokeColor == "none") "#000000" else strokeColor
        val sWidth = if (strokeWidth == 0.0) 1.0 else strokeWidth

        elements += "<rect " +
                "x=\"${format(x)}\" " +
                "y=\"${format(y)}\" " +
                "width=\"${format(width)}\" " +
                "height=\"${format(height)}\" " +
                "fill=\"none\" stroke=\"$sColor\" " +
                "stroke-width=\"${format(sWidth)}\" " +
                "/>\n"
    }

    override fun fillRectangle(x: Double, y: Double, width: Double, height: Double) {
        elements += "<rect " +
                "x=\"${format(x)}\" " +
                "y=\"${format(y)}\" " +
                "width=\"${format(width)}\" " +
                "height=\"${format(height)}\" " +
                "fill=\"$fillColor\" " +
                "stroke=\"$strokeColor\" " +
                "stroke-width=\"${format(strokeWidth)}\" " +
                "/>\n"
    }

    override fun drawCircle(x: Double, y: Double, radius: Double) {
        val sColor = if (strokeColor == "none") "#000000" else strokeColor
        val sWidth = if (strokeWidth == 0.0) 1.0 else strokeWidth

        elements += "<circle " +
                "cx=\"${format(x)}\" " +
                "cy=\"${format(y)}\" " +
                "r=\"${format(radius)}\" " +
                "fill=\"none\" " +
                "stroke=\"$sColor\" " +
                "stroke-width=\"${format(sWidth)}\" " +
                "/>\n"
    }

    override fun fillCircle(x: Double, y: Double, radius: Double) {
        elements += "<circle " +
                "cx=\"${format(x)}\" " +
                "cy=\"${format(y)}\" " +
                "r=\"${format(radius)}\" " +
                "fill=\"$fillColor\" " +
                "stroke=\"$strokeColor\" " +
                "stroke-width=\"${format(strokeWidth)}\" " +
                "/>\n"
    }

    override fun drawPolygon(points: List<Point>) {
        if (points.size < 3) return
        val pointsStr = points.joinToString(" ") { "${format(it.x)},${format(it.y)}" }
        val sColor = if (strokeColor == "none") "#000000" else strokeColor
        val sWidth = if (strokeWidth == 0.0) 1.0 else strokeWidth

        elements += "<polygon " +
                "points=\"$pointsStr\" " +
                "fill=\"none\" " +
                "stroke=\"$sColor\" " +
                "stroke-width=\"${format(sWidth)}\" " +
                "/>\n"
    }

    override fun fillPolygon(points: List<Point>) {
        if (points.size < 3) return
        val pointsStr = points.joinToString(" ") { "${format(it.x)},${format(it.y)}" }

        elements += "<polygon " +
                "points=\"$pointsStr\" " +
                "fill=\"$fillColor\" " +
                "stroke=\"$strokeColor\" " +
                "stroke-width=\"${format(strokeWidth)}\" " +
                "/>\n"
    }

    override fun drawText(text: String, x: Double, y: Double) {
        elements += "<text " +
                "x=\"${format(x)}\" " +
                "y=\"${format(y)}\" " +
                "font-family=\"$fontFamily\" " +
                "font-size=\"${format(fontSize)}\" " +
                "fill=\"$strokeColor\"" +
                ">" + text + "</text>\n"
    }

    override fun setStrokeColor(color: Color) {
        strokeColor = color.toHexColor()
    }

    override fun setFillColor(color: Color) {
        fillColor = color.toHexColor()
    }

    override fun setStrokeWidth(width: Double) {
        strokeWidth = width
    }

    override fun setFont(fontFamily: String, fontSize: Double) {
        this.fontFamily = fontFamily
        this.fontSize = fontSize
    }

    override fun toSvg(): String {
        val sb = StringBuilder()
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 5000 5000\">\n")
        elements.forEach { sb.append(it) }
        sb.append("</svg>\n")
        return sb.toString()
    }

    override fun clear() {
        elements.clear()
    }
}

fun main() {
    val g = SvgGraphics()
    g.setStrokeColor(Color.BLUE)
    g.setFont("Verdana", 24.0)
    g.drawText("Hello World", 100.0, 200.0)

    println(g.toSvg())
}