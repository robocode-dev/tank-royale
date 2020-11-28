package dev.robocode.tankroyale.gui.ui.fx

import java.awt.Graphics2D
import kotlin.math.sqrt

class Explosion(
    x: Double,
    y: Double,
    private val radius: Int,
    period: Int,
    numberOfCircles: Int,
    startTime: Int
) : Animation {

    private val smallBurstRadius = if (numberOfCircles == 1) radius.toDouble() else radius * 0.75
    private val parts = ArrayList<CircleBurst>()

    init {
        for (i in 1..numberOfCircles) {
            var cx = x
            var cy = y
            if (i > 1) {
                cx += radiusRandom()
                cy += radiusRandom()
            }
            parts.add(
                CircleBurst(
                    cx, cy,
                    smallBurstRadius * .1,
                    smallBurstRadius,
                    period,
                    startTime + (Math.random() * period * .3).toInt()
                )
            )
        }
    }

    override fun isFinished(): Boolean {
        return parts.count { it.finished } == parts.size
    }

    override fun paint(g: Graphics2D, time: Int) {
        for (part in parts) {
            part.paint(g, time)
        }
    }

    private fun radiusRandom(): Double {
        var r = radius - smallBurstRadius
        r *= 1.0 - sqrt(Math.random())
        return if (Math.random() > 0.5) r else -r
    }
}