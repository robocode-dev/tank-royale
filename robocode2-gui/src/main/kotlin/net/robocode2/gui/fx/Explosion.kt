package net.robocode2.gui.fx

import java.awt.Graphics2D

class Explosion(
        x: Double,
        y: Double,
        private val radius: Int,
        period: Int,
        numberOfCircles: Int,
        startTime: Int
) {
    var done: Boolean = false
        set(value) {
            parts.forEach { it.done = value }
            field = value
        }

    private val smallBurstRadius = radius * 0.75
    private val parts = ArrayList<CircleBurst>()

    init {
        for (i in 1..numberOfCircles) {
            var cx = x
            var cy = y
            if (i > 1) {
                cx += radiusRandom()
                cy += radiusRandom()
            }
            parts.add(CircleBurst(
                    cx, cy,
                    smallBurstRadius * .1,
                    smallBurstRadius,
                    period,
                    startTime + (Math.random() * period * .3).toInt()
            ))
        }
    }

    fun update(g: Graphics2D, time: Int) {
        var count = parts.size

        for (part in parts) {
            part.update(g, time)

            if (part.done) {
                count--
            }
        }
        done = count == 0
    }

    private fun radiusRandom(): Double {
        var r = radius - smallBurstRadius
        r *= 1.0 - Math.sqrt(Math.random())
        return if (Math.random() > 0.5) r else -r
    }
}

