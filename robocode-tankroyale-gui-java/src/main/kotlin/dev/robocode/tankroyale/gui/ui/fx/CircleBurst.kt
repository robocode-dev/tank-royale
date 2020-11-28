package dev.robocode.tankroyale.gui.ui.fx

import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.Ellipse2D

open class CircleBurst(
    var x: Double,
    var y: Double,
    private val startRadius: Double,
    private val endRadius: Double,
    private val period: Int,
    private val startTime: Int
) : Animation {

    var finished = false

    private val colors = arrayOf(
        Color.LIGHT_GRAY,
        Color.WHITE,
        Color.YELLOW,
        Color.ORANGE,
        Color(127, 51, 0), // brown
        Color(120, 120, 120),
        Color(100, 100, 100),
        Color(80, 80, 80),
        Color(70, 70, 70),
        Color(60, 60, 60),
        Color(50, 50, 50),
        Color(40, 40, 40),
        Color(30, 30, 30),
        Color(20, 20, 20),
        Color(10, 10, 10),
        Color.BLACK
    )

    override fun paint(g: Graphics2D, time: Int) {
        if (time < startTime) return

        val dt = time - startTime
        val t = dt.toDouble() / period
        val r: Double = (startRadius + (endRadius - startRadius) * t).coerceAtMost(endRadius)

        val ct = (t * (colors.size - 1)).toInt()
        if (dt < period) {
            val colorA = colors[ct]
            val colorB = colors[ct + 1]

            g.color = lerpRGB(colorA, colorB, t, (1.0 - t))
            g.fillCircle(x, y, r)
        } else
            finished = true
    }

    override fun isFinished(): Boolean {
        return finished
    }

    private val circleShape = Area(Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0))

    private fun Graphics2D.fillCircle(x: Double, y: Double, radius: Double) {
        this.color = color
        val transform = AffineTransform.getTranslateInstance(x, y)
        transform.scale(radius, radius)
        fill(circleShape.createTransformedArea(transform))
    }

    private fun lerpRGB(a: Color, b: Color, t: Double, alpha: Double): Color {
        return Color(
            (a.red + (b.red - a.red) * t).toInt(),
            (a.green + (b.green - a.green) * t).toInt(),
            (a.blue + (b.blue - a.blue) * t).toInt(),
            (255 * alpha).toInt()
        )
    }
}