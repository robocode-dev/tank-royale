package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.model.BotState
import dev.robocode.tankroyale.gui.ui.arena.ColorConstant.DEFAULT_BODY_COLOR
import dev.robocode.tankroyale.gui.ui.arena.ColorConstant.DEFAULT_GUN_COLOR
import dev.robocode.tankroyale.gui.ui.arena.ColorConstant.DEFAULT_RADAR_COLOR
import dev.robocode.tankroyale.gui.ui.arena.ColorConstant.DEFAULT_TRACKS_COLOR
import dev.robocode.tankroyale.gui.ui.arena.ColorConstant.DEFAULT_TURRET_COLOR
import dev.robocode.tankroyale.gui.ui.extensions.ColorExt.lightness
import dev.robocode.tankroyale.gui.ui.extensions.ColorExt.hsl
import dev.robocode.tankroyale.gui.util.ColorUtil.Companion.fromString
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Color.BLACK
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import java.awt.geom.Rectangle2D
import kotlin.math.hypot

class Tank(private val bot: BotState) {

    var oldX = 0.0
    var oldY = 0.0

    private val tracksColor: Color = fromString(bot.tracksColor ?: DEFAULT_TRACKS_COLOR)

    fun paint(g: Graphics2D) {
        val oldTransform = g.transform

        val scale = 36.0 / 500
        g.translate(bot.x, bot.y)
        g.scale(scale, scale)

        paintBody(g)
        paintTurret(g)
        paintRadar(g)

        g.transform = oldTransform

        oldX = bot.x
        oldY = bot.y
    }

    private fun paintBody(g: Graphics2D) {
        val oldTransform = g.transform
        g.rotate(Math.toRadians(bot.direction + 180))

        // Tracks
        val localTransform = g.transform
        g.translate(-300, -250)
        paintTrack(g)
        g.transform = localTransform

        g.translate(-300, 115)
        paintTrack(g)
        g.transform = localTransform

        // Body rect
        val bodyColor = fromString(bot.bodyColor ?: DEFAULT_BODY_COLOR)
        g.color = bodyColor
        g.fillRect(-210, -160, 420, 320)

        // Body Shadow
        g.color = Color(0, 0, 0, 0x3F)
        g.fillRect(120, -160, 90, 320)

        // Body border
        g.paint = borderColor(bodyColor)
        g.stroke = BasicStroke(20f)
        g.drawRoundRect(-210, -160, 420, 320, 20, 20)

        g.transform = oldTransform
    }

    private fun paintTrack(g: Graphics2D) {
        val dx = bot.x - oldX
        val dy = bot.y - oldY

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

        val tracksColor = tracksColor.hsl.multLight(0.6f).toColor()
        g.color = tracksColor
        g.fillRect(75, 20, 450, 95)
        g.color = borderColor(tracksColor)
        g.drawRect(75, 20, 450, 95)
    }

    private fun paintLink0(g: Graphics2D) {
        g.stroke = BasicStroke(10f)

        g.color = tracksColor.hsl.addLight(0.3f).toColor()
        g.fillRect(55, 5, 25, 125)

        g.color = tracksColor
        g.fillRoundRect(70, 10, 35, 116, 20, 20)

        g.color = borderColor(tracksColor)
        g.drawRoundRect(55, 5, 50, 125, 20, 20)
    }

    private fun paintLink30(g: Graphics2D) {
        g.stroke = BasicStroke(10f)

        g.color = tracksColor.hsl.addLight(0.2f).multLight(0.866f /* 30 deg */).toColor()
        g.fillRect(55, 5, 25, 125)

        g.color = tracksColor.hsl.multLight(0.866f /* 30 deg */).toColor()
        g.fillRoundRect(70, 10, 30, 116, 20, 20)

        g.color = borderColor(tracksColor)
        g.drawRoundRect(55, 5, 42, 125, 20, 20)
    }

    private fun paintLink60(g: Graphics2D) {
        g.stroke = BasicStroke(10f)

        g.color = tracksColor.hsl.addLight(0.2f).multLight(0.5f /* 60 deg */).toColor()
        g.fillRect(55, 5, 20, 125)

        g.color = borderColor(tracksColor)
        g.drawRoundRect(55, 5, 20, 125, 20, 20)
    }

    private fun paintTurret(g: Graphics2D) {
        val oldTransform = g.transform
        g.rotate(Math.toRadians(bot.gunDirection + 180))

        // Cannon thick part

        val gunColor = fromString(bot.gunColor ?: DEFAULT_GUN_COLOR)

        val cannonLight = gunColor.hsl.addLight(0.1f).toColor()
        val cannonDark = gunColor.hsl.addLight(-0.3f).toColor()

        val borderColor = borderColor(gunColor)

        val x1 = -160f
        val y1 = -40f
        g.paint = GradientPaint(x1, y1 + 10, cannonDark, x1, y1 + 40, cannonLight)
        g.fill(Rectangle2D.Float(x1, y1, 80f, 40f))
        g.paint = GradientPaint(x1, y1 + 40, cannonLight, x1, y1 + 80 - 10, cannonDark)
        g.fill(Rectangle2D.Float(x1, y1 + 40, 80f, 40f))

        g.color = borderColor
        g.drawRect(x1.toInt(), y1.toInt(), 80, 80)

        // Cannon long part
        val x2 = -330f
        val y2 = -25f
        g.paint = GradientPaint(x2, y2 + 10, cannonDark, x2, y2 + 25, cannonLight)
        g.fill(Rectangle2D.Float(x2, y2, 170f, 25f))
        g.paint = GradientPaint(x2, y2 + 25, cannonLight, x2, y2 + 50 - 10, cannonDark)
        g.fill(Rectangle2D.Float(x2, y2 + 25, 170f, 25f))

        g.color = borderColor(cannonDark)
        g.drawRect(x2.toInt(), y2.toInt(), 170, 50)

        // Turret rect
        val turretColor = fromString(bot.turretColor ?: DEFAULT_TURRET_COLOR)
        g.color = turretColor
        g.fillRect(-80, -100, 200, 200)

        // Turret shadow
        g.color = Color(0, 0, 0, 0x5F)
        g.fillRect(60, -100, 50, 200)

        // Turret border
        g.color = borderColor(turretColor)
        g.stroke = BasicStroke(20f)
        g.drawRoundRect(-80, -100, 200, 200, 20, 20)

        g.transform = oldTransform
    }

    private fun paintRadar(g: Graphics2D) {
        if (bot.isDroid) {
            return // Droids do not have a radar
        }

        val oldTransform = g.transform

        val color = fromString(bot.radarColor ?: DEFAULT_RADAR_COLOR)
        val borderColor = borderColor(color)

        // Circle
        val circle = Ellipse2D.Float(-30f, -30f, 60f, 60f)
        g.color = color
        g.fill(circle)
        g.color = borderColor
        g.draw(circle)

        // Radar
        g.rotate(Math.toRadians(bot.radarDirection + 180))
        val path = GeneralPath()
        path.moveTo(20.0, -110.0)
        path.quadTo(120.0, 0.0, 20.0, 110.0)
        path.closePath()

        g.color = color
        g.fill(path)
        g.color = borderColor
        g.draw(path)

        g.transform = oldTransform
    }

    private fun borderColor(color: Color): Color {
        return if (color.lightness < 0.15) Color.DARK_GRAY else BLACK
    }
}