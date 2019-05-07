package net.robocode2.gui.ui.battle

import net.robocode2.gui.client.Client
import net.robocode2.gui.fx.Animation
import net.robocode2.gui.fx.CircleBurst
import net.robocode2.gui.fx.Explosion
import net.robocode2.gui.model.*
import net.robocode2.gui.ui.ResultsWindow
import net.robocode2.gui.utils.Graphics2DState
import java.awt.*
import java.awt.event.MouseWheelEvent
import java.awt.geom.*
import javax.swing.JPanel


class ArenaPanel : JPanel() {

    private var scale = 1.0

    private val circleShape = Area(Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0))

    private val explosions = ArrayList<Animation>()

    private companion object State {
        var arenaWidth: Int = 800
        var arenaHeight: Int = 600

        var time: Int = 0
        var bots: Set<BotState> = HashSet()
        var bullets: Set<BulletState> = HashSet()
    }

    private val state = State

    init {
        addMouseWheelListener { e -> if (e != null) onMouseWheel(e) }

        Client.onGameStarted.subscribe { onGameStarted(it) }
        Client.onGameEnded.subscribe { onGameEnded(it) }
        Client.onGameAborted.subscribe { onGameAborted(it) }
        Client.onTickEvent.subscribe { onTick(it) }
    }

    private fun onGameStarted(gameStartedEvent: GameStartedEvent) {
        val setup = gameStartedEvent.gameSetup
        state.arenaWidth = setup.arenaWidth
        state.arenaHeight = setup.arenaHeight
    }

    private fun onGameEnded(gameEndedEvent: GameEndedEvent) {
        ResultsWindow(gameEndedEvent.results).isVisible = true
    }

    private fun onGameAborted(gameAbortedEvent: GameAbortedEvent) {
        // TODO
    }

    private fun onTick(tickEvent: TickEvent) {
        state.time = tickEvent.roundState.turnNumber
        state.bots = tickEvent.botStates
        state.bullets = tickEvent.bulletStates

        tickEvent.events.forEach { event ->
            when (event) {
                is BotDeathEvent -> onBotDeath(event)
                is BulletHitBotEvent -> onBulletHitBot(event)
                is BulletHitWallEvent -> onBulletMissed(event)
                is BulletHitBulletEvent -> onBulletHitBullet(event)
            }
        }
        repaint()
    }

    private fun onBotDeath(botDeathEvent: BotDeathEvent) {
        val bot = bots.first { bot -> bot.id == botDeathEvent.victimId }
        val explosion = Explosion(bot.x, bot.y, 80, 50, 15, state.time)
        explosions.add(explosion)
    }

    private fun onBulletHitBot(bulletHitBotEvent: BulletHitBotEvent) {
        val bullet = bulletHitBotEvent.bullet
        val bot = bots.first { bot -> bot.id == bulletHitBotEvent.victimId }

        val xOffset = bot.x - bullet.x
        val yOffset = bot.y - bullet.y

        val explosion = BotHitExplosion(bot.x, bot.y, xOffset, yOffset, bot.id, 4.0, 40.0, 25, state.time)
        explosions.add(explosion)
    }

    private fun onBulletMissed(bulletHitWallEvent: BulletHitWallEvent) {
        val bullet = bulletHitWallEvent.bullet
        val explosion = CircleBurst(bullet.x, bullet.y, 4.0, 40.0, 25, state.time)
        explosions.add(explosion)
    }

    private fun onMouseWheel(e: MouseWheelEvent) {
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

    private fun onBulletHitBullet(bulletHitBulletEvent: BulletHitBulletEvent) {
        val bullet1 = bulletHitBulletEvent.bullet
        val bullet2 = bulletHitBulletEvent.hitBullet

        val x = (bullet1.x + bullet2.x) / 2
        val y = (bullet1.y + bullet2.y) / 2

        val explosion = CircleBurst(x, y, 4.0, 40.0, 25, state.time)
        explosions.add(explosion)
    }

    override fun paintComponent(g: Graphics) {
        (g as Graphics2D).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        try {
            drawArena(g)
        } finally {
            g.dispose()
        }
    }

    private fun drawArena(g: Graphics2D) {
        clearCanvas(g)

        val marginX = (size.width - scale * arenaWidth) / 2
        val marginY = (size.height - scale * arenaHeight) / 2

        g.translate(marginX, marginY)
        g.scale(scale, scale)

        drawGround(g)
        drawBots(g)
        drawExplosions(g)
        drawBullets(g)
    }

    private fun drawBots(g: Graphics2D) {
        state.bots.forEach {
            val x = it.x
            val y = it.y

            drawBotBody(g, x, y, it.direction, Color.BLUE)
            drawGun(g, x, y, it.gunDirection)
            drawRadar(g, x, y, it.radarDirection, Color.RED)
            drawScanArc(g, x, y, it.radarDirection, it.radarSweep, Color.WHITE)
            drawEnergy(g, x, y, it.energy)
        }
    }

    private fun drawBullets(g: Graphics2D) {
        state.bullets.forEach {
            drawBullet(g, it.x, it.y, it.power)
        }
    }

    private fun clearCanvas(g: Graphics) {
        g.color = Color.DARK_GRAY
        g.fillRect(0, 0, size.width, size.height)
    }

    private fun drawGround(g: Graphics) {
        g.color = Color.BLACK
        g.fillRect(0, 0, state.arenaWidth, state.arenaHeight)
    }

    private fun drawExplosions(g: Graphics2D) {
        val it = explosions.iterator()
        while (it.hasNext()) {
            val explosion = it.next()

            if (explosion is BotHitExplosion) {
                val bot = bots.firstOrNull() { bot -> bot.id == explosion.victimId }
                if (bot != null) {
                    explosion.x = bot.x
                    explosion.y = bot.y
                }
            }

            explosion.paint(g, state.time)
            if (explosion.isFinished()) {
                it.remove()
            }
        }
    }

    private fun drawBullet(g: Graphics2D, x: Double, y: Double, power: Double) {
        val size = 2 * Math.sqrt(2.5 * power)
        g.color = Color.WHITE
        g.fillCircle(x, y, size)
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

        g.color = Color.LIGHT_GRAY
        g.fillCircle(0.0, 0.0, 18.0)

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

        path.moveTo(10.0 - 2, -10.0)
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

    private fun drawEnergy(g: Graphics2D, x: Double, y: Double, energy: Double) {
        val oldState = Graphics2DState(g)

        g.color = Color.WHITE
        val text = "%.1f".format(energy)
        val width = g.fontMetrics.stringWidth(text)
        g.drawString(text, x.toFloat() - width / 2, (y - 30).toFloat())

        oldState.restore(g)
    }

    private fun Graphics2D.fillCircle(x: Double, y: Double, size: Double) {
        this.color = color
        val transform = AffineTransform.getTranslateInstance(x, y)
        transform.scale(size, size)
        fill(circleShape.createTransformedArea(transform))
    }

    class BotHitExplosion(
            x: Double,
            y: Double,
            private val xOffset: Double,
            private val yOffset: Double,
            val victimId: Int,
            startRadius: Double,
            endRadius: Double,
            period: Int,
            startTime: Int
    ) : CircleBurst(x, y, startRadius, endRadius, period, startTime) {

        override fun paint(g: Graphics2D, time: Int) {

            val origX = x
            val origY = y

            x += xOffset
            y += yOffset

            super.paint(g, time)

            x = origX
            y = origY
        }
    }
}