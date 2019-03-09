package net.robocode2.gui.ui.battle

import net.robocode2.gui.utils.Graphics2DState
import java.awt.*
import java.awt.event.MouseWheelEvent
import java.awt.geom.*
import javax.swing.JPanel


class ArenaPanel : JPanel() {

    private var scale = 1.0

    private val CIRCLE_SHAPE = Area(Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0))

    init {
        addMouseWheelListener { e -> if (e != null) onMouseWheel(e) }
    }

    fun onMouseWheel(e: MouseWheelEvent) {
        var newScale = scale
        if (e.unitsToScroll > 0) {
            newScale *= 1.2
        } else if (e.unitsToScroll < 0) {
            newScale /= 1.2
        }
        if (newScale != scale && newScale >= 0.25 && newScale <= 10) {
            scale = newScale
            repaint()
        }
    }

    override fun paintComponent(g: Graphics) {
        try {
            drawArena(g as Graphics2D)
        } finally {
            g.dispose()
        }
    }

    private fun drawArena(g: Graphics2D) {
        clearCanvas(g)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g.scale(scale, scale)

        drawBotBody(g,18.5, 18.5, 0.0, Color.BLUE)
        drawGun(g, 18.5, 18.5, 0.0)
        drawRadar(g,18.5, 18.5, 45.0, Color.RED)
        drawScanArc(g, 18.5, 18.5, 45.0, 45.0, Color.WHITE)
    }

    private fun clearCanvas(g: Graphics) {
        g.color = Color.BLACK
        g.fillRect(0, 0, size.width, size.height)
    }

    private fun drawBullet(g: Graphics2D, x: Double, y: Double, power: Double) {
        val size = 2 * Math.sqrt(2.5 * power)
        fillCircle(g, x, y, size, Color.WHITE)
    }

    private fun fillCircle(g: Graphics2D, x: Double, y: Double, size: Double, color: Color) {
        val transform = AffineTransform.getTranslateInstance(x, y)
        transform.scale(size, size)
        val transformedCircle = CIRCLE_SHAPE.createTransformedArea(transform)

        g.color = color
        g.fill(transformedCircle)
    }

    private fun drawBotBody(g: Graphics2D, x: Double, y: Double, direction: Double, color: Color) {
        val oldState = Graphics2DState(g)

        g.translate(x, y)
        g.rotate(Math.toRadians(direction))

        g.color = color
        g.fillRect(-18, -18 + 1 + 6, 36, 36 - 2 * 7)

        g.color = Color.GRAY

        g.fillRect(-18, -18, 36, 6)
        g.fillRect(-18, 18 - 6, 36, 6)

        oldState.restore(g)
    }

    private fun drawGun(g: Graphics2D, x: Double, y: Double, direction: Double) {
        val oldState = Graphics2DState(g)

        g.translate(x, y)

        fillCircle(g,0.0, 0.0, 18.0, Color.LIGHT_GRAY)

        g.rotate(Math.toRadians(direction))
        g.fillRect(8, -2, 16, 4)

        oldState.restore(g)
    }

    private fun drawRadar(g: Graphics2D, x: Double, y: Double, direction: Double, color: Color) {
        val oldState = Graphics2DState(g)

        g.translate(x, y)
        g.rotate(Math.toRadians(direction))

        g.color = color

        val path = GeneralPath()
        path.moveTo(8.0, 10.0)
        path.curveTo(-2.0, 10.0, -2.0, -10.0, 8.0, -10.0)

        path.moveTo(10.0-2, -10.0)
        path.curveTo(-9.0, -10.0, -9.0, 10.0, 8.0, 10.0)
        path.closePath()

        g.fill(path)

        oldState.restore(g)
    }

    private fun drawScanArc(g: Graphics2D, x: Double, y: Double, direction: Double, spreadAngle: Double, color: Color) {
        val oldState = Graphics2DState(g)

        g.color = color
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f)

        val arc = Arc2D.Double()
        arc.setArcByCenter(x, y, 1200.0, (360 - direction) - spreadAngle / 2, spreadAngle, Arc2D.PIE)
        g.fill(arc)

        oldState.restore(g)
    }
}