package net.robocode2.gui.fx

import java.awt.Graphics2D

class Explosion(
    x: Double,
    y: Double,
    private val radius: Int,
    period: Int,
    numberOfCircles: Int,
    startTime: Int
) : Animation {
    var finished: Boolean = false
        set(value) {
            parts.forEach { it.finished = value }
            field = value
        }

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

    override fun paint(g: Graphics2D, time: Int) {
        var count = parts.size

        for (part in parts) {
            part.paint(g, time)

            if (part.finished) {
                count--
            }
        }
        finished = count == 0
    }

    override fun isFinished(): Boolean {
        return finished
    }

    private fun radiusRandom(): Double {
        var r = radius - smallBurstRadius
        r *= 1.0 - Math.sqrt(Math.random())
        return if (Math.random() > 0.5) r else -r
    }
}

