package dev.robocode.tankroyale.botapi.graphics

import kotlin.math.round

class SvgGraphics : IGraphics {
    private val elements = mutableListOf<String>()

    private var strokeColor: Color = Color.BLACK
    private var strokeWidth: Double = 1.0
    private var fillColor: Color = Color.TRANSPARENT

    private var fontFamily: String? = null
    private var fontSize: Double? = null

    override fun setStrokeColor(color: Color) {
        strokeColor = color
    }

    override fun setStrokeWidth(width: Double) {
        strokeWidth = width
    }

    override fun setFillColor(color: Color) {
        fillColor = color
    }

    override fun setFont(family: String, size: Double) {
        fontFamily = family
        fontSize = size
    }

    override fun clear() {
        elements.clear()
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        val sb = StringBuilder()
        sb.append("<line ")
        sb.append("x1=\"").append(formatNumber(x1)).append("\" ")
        sb.append("y1=\"").append(formatNumber(y1)).append("\" ")
        sb.append("x2=\"").append(formatNumber(x2)).append("\" ")
        sb.append("y2=\"").append(formatNumber(y2)).append("\" ")
        sb.append("stroke=\"").append(hexNoAlpha(strokeColor)).append("\" ")
        sb.append("stroke-width=\"").append(formatNumber(strokeWidth)).append("\" ")
        sb.append("/>")
        elements.add(sb.toString())
    }

    override fun drawRectangle(x: Double, y: Double, width: Double, height: Double) {
        val sb = StringBuilder()
        sb.append("<rect ")
        sb.append("x=\"").append(formatNumber(x)).append("\" ")
        sb.append("y=\"").append(formatNumber(y)).append("\" ")
        sb.append("width=\"").append(formatNumber(width)).append("\" ")
        sb.append("height=\"").append(formatNumber(height)).append("\" ")
        sb.append("fill=\"none\" ")
        sb.append("stroke=\"").append(hexNoAlpha(strokeColor)).append("\" ")
        sb.append("stroke-width=\"").append(formatNumber(strokeWidth)).append("\" ")
        sb.append("/>")
        elements.add(sb.toString())
    }

    override fun fillRectangle(x: Double, y: Double, width: Double, height: Double) {
        val sb = StringBuilder()
        sb.append("<rect ")
        sb.append("x=\"").append(formatNumber(x)).append("\" ")
        sb.append("y=\"").append(formatNumber(y)).append("\" ")
        sb.append("width=\"").append(formatNumber(width)).append("\" ")
        sb.append("height=\"").append(formatNumber(height)).append("\" ")
        sb.append("fill=\"").append(hex(fillColor)).append("\" ")
        sb.append("stroke=\"").append(hexNoAlpha(strokeColor)).append("\" ")
        sb.append("stroke-width=\"").append(formatNumber(strokeWidth)).append("\" ")
        sb.append("/>")
        elements.add(sb.toString())
    }

    override fun drawCircle(cx: Double, cy: Double, r: Double) {
        val sb = StringBuilder()
        sb.append("<circle ")
        sb.append("cx=\"").append(formatNumber(cx)).append("\" ")
        sb.append("cy=\"").append(formatNumber(cy)).append("\" ")
        sb.append("r=\"").append(formatNumber(r)).append("\" ")
        sb.append("fill=\"none\" ")
        sb.append("stroke=\"").append(hexNoAlpha(strokeColor)).append("\" ")
        sb.append("stroke-width=\"").append(formatNumber(strokeWidth)).append("\" ")
        sb.append("/>")
        elements.add(sb.toString())
    }

    override fun fillCircle(cx: Double, cy: Double, r: Double) {
        val sb = StringBuilder()
        sb.append("<circle ")
        sb.append("cx=\"").append(formatNumber(cx)).append("\" ")
        sb.append("cy=\"").append(formatNumber(cy)).append("\" ")
        sb.append("r=\"").append(formatNumber(r)).append("\" ")
        sb.append("fill=\"").append(hex(fillColor)).append("\" ")
        sb.append("stroke=\"").append(hexNoAlpha(strokeColor)).append("\" ")
        sb.append("stroke-width=\"").append(formatNumber(strokeWidth)).append("\" ")
        sb.append("/>")
        elements.add(sb.toString())
    }

    override fun drawPolygon(points: List<Point>) {
        if (points.size < 3) return
        val sb = StringBuilder()
        sb.append("<polygon ")
        sb.append("points=\"")
        sb.append(points.joinToString(" ") { p -> "${formatNumber(p.x)},${formatNumber(p.y)}" })
        sb.append("\" ")
        sb.append("fill=\"none\" ")
        sb.append("stroke=\"").append(hexNoAlpha(strokeColor)).append("\" ")
        sb.append("stroke-width=\"").append(formatNumber(strokeWidth)).append("\" ")
        sb.append("/>")
        elements.add(sb.toString())
    }

    override fun fillPolygon(points: List<Point>) {
        if (points.size < 3) return
        val sb = StringBuilder()
        sb.append("<polygon ")
        sb.append("points=\"")
        sb.append(points.joinToString(" ") { p -> "${formatNumber(p.x)},${formatNumber(p.y)}" })
        sb.append("\" ")
        sb.append("fill=\"").append(hex(fillColor)).append("\" ")
        sb.append("stroke=\"").append(hexNoAlpha(strokeColor)).append("\" ")
        sb.append("stroke-width=\"").append(formatNumber(strokeWidth)).append("\" ")
        sb.append("/>")
        elements.add(sb.toString())
    }

    override fun drawText(text: String, x: Double, y: Double) {
        val sb = StringBuilder()
        sb.append("<text ")
        sb.append("x=\"").append(formatNumber(x)).append("\" ")
        sb.append("y=\"").append(formatNumber(y)).append("\" ")
        fontFamily?.let { sb.append("font-family=\"$it\" ") }
        fontSize?.let { sb.append("font-size=\"").append(formatNumber(it)).append("\" ") }
        // According to tests, text fill should follow stroke color
        sb.append("fill=\"").append(hexNoAlpha(strokeColor)).append("\"")
        sb.append(">")
        sb.append(escapeXml(text))
        sb.append("</text>")
        elements.add(sb.toString())
    }

    override fun toSvg(): String {
        val sb = StringBuilder()
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 5000 5000\">")
        if (elements.isNotEmpty()) {
            sb.append("\n")
            elements.forEach { el -> sb.append(el).append("\n") }
        }
        sb.append("</svg>")
        return sb.toString()
    }

    private fun formatNumber(value: Double): String {
        // Round to 3 decimals
        val scaled = round(value * 1000.0).toLong()
        val negative = scaled < 0
        val absScaled = kotlin.math.abs(scaled)
        val whole = absScaled / 1000
        val frac = absScaled % 1000
        val fracStr = if (frac == 0L) "" else frac.toString().padStart(3, '0').trimEnd('0')
        val sign = if (negative) "-" else ""
        return if (fracStr.isEmpty()) "$sign$whole" else "$sign$whole.$fracStr"
    }

    private fun hex(color: Color): String = color.toHexColor()

    private fun hexNoAlpha(color: Color): String = Color.fromRgba(color, 255).toHexColor()

    private fun escapeXml(text: String): String {
        return buildString(text.length) {
            for (ch in text) {
                when (ch) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&apos;")
                    else -> append(ch)
                }
            }
        }
    }
}
