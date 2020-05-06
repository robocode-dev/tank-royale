package dev.robocode.tankroyale.ui.desktop.ui.arena

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Color.BLACK
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import java.awt.geom.Rectangle2D
import kotlin.math.hypot

class Tank(
    private val x: Double,
    private val y: Double,
    private val direction: Double,
    private val gunDirection: Double,
    private val radarDirection: Double
) {

    var oldX = 0.0
    var oldY = 0.0

    fun paint(g: Graphics2D) {
        val oldTransform = g.transform

        val scale = 36.0 / 500
        g.translate(x, y)
        g.scale(scale, scale)

        paintBody(g, Color(0x001199))
        paintTurret(g, Color(0x0066CC))
        paintRadar(g, Color(0xAAAAFF))

        g.transform = oldTransform

        oldX = x
        oldY = y
    }

    private fun paintBody(g: Graphics2D, color: Color) {
        val oldTransform = g.transform
        g.rotate(Math.toRadians(direction + 180))

        // Tracks
        val localTransform = g.transform
        g.translate(-300, -250)
        paintTrack(g)
        g.transform = localTransform

        g.translate(-300, 115)
        paintTrack(g)
        g.transform = localTransform

        // Body rect
        g.color = color
        g.fillRect(-210, -160, 420, 320)

        // Body Shadow
        g.color = Color(0, 0, 0, 0x7F)
        g.fillRect(120, -160, 90, 320)

        // Body border
        g.paint = BLACK
        g.stroke = BasicStroke(20f)
        g.drawRoundRect(-210, -160, 420, 320, 20, 20)

        g.transform = oldTransform
    }

    private fun paintTrack(g: Graphics2D) {
        val dx = x - oldX
        val dy = y - oldY

        val dist = hypot(dx, dy)
        val mod: Int = ((dist / 10) % 3).toInt()

        if (mod == 0) {
            paintTrack1(g)
        } else if (mod == 1) {
            paintTrack2(g)
        } else {
            paintTrack3(g)
        }
    }

    private fun paintTrack1(g: Graphics2D) {
        paintMainTrack(g)
        paintLink30(g)
        g.translate(60, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink0(g)
        g.translate(68, 0)
        paintLink30(g)
    }

    private fun paintTrack2(g: Graphics2D) {
        paintMainTrack(g)
        paintLink60(g)
        g.translate(33, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink30(g)
    }

    private fun paintTrack3(g: Graphics2D) {
        paintMainTrack(g)
        g.translate(7, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink0(g)
        g.translate(80, 0)
        paintLink0(g)
        g.translate(63, 0)
        paintLink60(g)
    }

    private fun paintMainTrack(g: Graphics2D) {
        g.stroke = BasicStroke(10f)

        g.color = Color(0x333333)
        g.fillRect(75, 20, 450, 95)
        g.color = BLACK
        g.drawRect(75, 20, 450, 95)
    }

    private fun paintLink0(g: Graphics2D) {
        g.stroke = BasicStroke(10f)

        g.color = Color(0xAAAAAA)
        g.fillRect(55, 5, 25, 125)

        g.color = Color(0x666666)
        g.fillRoundRect(70, 10, 35, 116, 20, 20)

        g.color = BLACK
        g.drawRoundRect(55, 5, 50, 125, 20, 20)
    }

    private fun paintLink30(g: Graphics2D) {
        g.stroke = BasicStroke(10f)

        g.color = Color(0x888888)
        g.fillRect(55, 5, 25, 125)

        g.color = Color(0x444444)
        g.fillRoundRect(70, 10, 30, 116, 20, 20)

        g.color = BLACK
        g.drawRoundRect(55, 5, 42, 125, 20, 20)
    }

    private fun paintLink60(g: Graphics2D) {
        g.stroke = BasicStroke(10f)

        g.color = Color(0x444444)
        g.fillRect(55, 5, 20, 125)

        g.color = BLACK
        g.drawRoundRect(55, 5, 20, 125, 20, 20)
    }

    private fun paintTurret(g: Graphics2D, color: Color) {
        val oldTransform = g.transform
        g.rotate(Math.toRadians(gunDirection + 180))

        // Turret rect
        g.color = color
        g.fillRect(-80, -100, 200, 200)

        // Turret shadow
        g.color = Color(0, 0, 0, 0x7F)
        g.fillRect(60, -100, 50, 200)

        // Turret border
        g.color = BLACK
        g.stroke = BasicStroke(20f)
        g.drawRoundRect(-80, -100, 200, 200, 20, 20)

        // Cannon thick part
        val x1 = -160f
        val y1 = -40f
        g.paint = GradientPaint(x1, y1 + 10, Color(0x333333), x1, y1 + 40, Color(0xCCCCCC))
        g.fill(Rectangle2D.Float(x1, y1, 80f, 40f))
        g.paint = GradientPaint(x1, y1 + 40, Color(0xCCCCCC), x1, y1 + 80 - 10, Color(0x333333))
        g.fill(Rectangle2D.Float(x1, y1 + 40, 80f, 40f))

        g.color = BLACK
        g.drawRect(x1.toInt(), y1.toInt(), 80, 80)

        // Cannon long part
        val x2 = -330f
        val y2 = -25f
        g.paint = GradientPaint(x2, y2 + 10, Color(0x333333), x2, y2 + 25, Color(0xCCCCCC))
        g.fill(Rectangle2D.Float(x2, y2, 170f, 25f))
        g.paint = GradientPaint(x2, y2 + 25, Color(0xCCCCCC), x2, y2 + 50 - 10, Color(0x333333))
        g.fill(Rectangle2D.Float(x2, y2 + 25, 170f, 25f))

        g.color = BLACK
        g.drawRect(x2.toInt(), y2.toInt(), 170, 50)

        g.transform = oldTransform

    }

    private fun paintRadar(g: Graphics2D, color: Color) {
        val oldTransform = g.transform

        // Circle
        val circle = Ellipse2D.Float(-30f, -30f, 60f, 60f)
        g.color = color
        g.fill(circle)
        g.color = BLACK
        g.draw(circle)

        // Radar
        g.rotate(Math.toRadians(radarDirection + 180))
        val path = GeneralPath()
        path.moveTo(20.0, -110.0)
        path.quadTo(120.0, 0.0, 20.0, 110.0)
        path.closePath()

        g.color = color
        g.fill(path)
        g.color = BLACK
        g.draw(path)

        g.transform = oldTransform
    }
}