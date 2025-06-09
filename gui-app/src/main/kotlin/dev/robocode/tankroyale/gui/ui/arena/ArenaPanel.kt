package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.ui.ResultsFrame
import dev.robocode.tankroyale.gui.ui.extensions.ColorExt.hsl
import dev.robocode.tankroyale.gui.ui.extensions.ColorExt.lightness
import dev.robocode.tankroyale.gui.ui.fx.Animation
import dev.robocode.tankroyale.gui.ui.fx.CircleBurst
import dev.robocode.tankroyale.gui.ui.fx.Explosion
import dev.robocode.tankroyale.gui.ui.svg.SvgToGraphicsRender
import dev.robocode.tankroyale.gui.util.ColorUtil.Companion.fromString
import dev.robocode.tankroyale.gui.util.Graphics2DState
import dev.robocode.tankroyale.gui.util.HslColor
import java.awt.*
import java.awt.event.*
import java.awt.geom.AffineTransform
import java.awt.geom.Arc2D
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JPanel
import kotlin.math.sqrt


object ArenaPanel : JPanel() {

    private val circleShape = Area(Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0))

    private val explosions = CopyOnWriteArrayList<Animation>()

    private var arenaWidth: Int = Client.currentGameSetup?.arenaWidth ?: 800
    private var arenaHeight: Int = Client.currentGameSetup?.arenaHeight ?: 600

    private var round: Int = 0
    private var time: Int = 0
    private var bots: Set<BotState> = HashSet()
    private var bullets: Set<BulletState> = HashSet()

    private val tick = AtomicBoolean(false)

    private var scale = 1.0

    init {
        addMouseWheelListener { e -> if (e != null) onMouseWheel(e) }

        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                onMousePressed(e)
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                onMouseDragged(e)
            }
        })

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                recalcScale()
            }
        })

        ClientEvents.apply {
            onGameEnded.subscribe(ArenaPanel) { onGameEnded(it) }
            onTickEvent.subscribe(ArenaPanel) { onTick(it) }
            onGameStarted.subscribe(ArenaPanel) { onGameStarted(it) }
        }
    }

    private fun onGameEnded(gameEndedEvent: GameEndedEvent) {
        ResultsFrame(gameEndedEvent.results).isVisible = true
    }

    private fun onTick(tickEvent: TickEvent) {
        if (tick.get()) return
        tick.set(true)

        if (tickEvent.turnNumber == 1) {
            // Make sure to remove any explosion left from earlier battle
            synchronized(explosions) {
                explosions.clear()
            }
        }

        round = tickEvent.roundNumber
        time = tickEvent.turnNumber
        bots = tickEvent.botStates
        bullets = tickEvent.bulletStates

        tickEvent.events.forEach {
            when (it) {
                is BotDeathEvent -> onBotDeath(it)
                is BulletHitBotEvent -> onBulletHitBot(it)
                is BulletHitWallEvent -> onBulletHitWall(it)
                is BulletHitBulletEvent -> onBulletHitBullet(it)
                else -> {
                    // ignore other events
                }
            }
        }

        repaint()

        tick.set(false)
    }

    private fun onGameStarted(gameStartedEvent: GameStartedEvent) {
        gameStartedEvent.gameSetup.apply {
            ArenaPanel.arenaWidth = arenaWidth
            ArenaPanel.arenaHeight = arenaHeight
        }

        recalcScale()
        repaint()
    }

    private fun onBotDeath(botDeathEvent: BotDeathEvent) {
        val bot = bots.first { bot -> bot.id == botDeathEvent.victimId }
        val explosion = Explosion(bot.x, bot.y, 80, 50, 15, time)
        synchronized(explosions) {
            explosions.add(explosion)
        }
    }

    private fun onBulletHitBot(bulletHitBotEvent: BulletHitBotEvent) {
        val bullet = bulletHitBotEvent.bullet
        val bot = bots.first { bot -> bot.id == bulletHitBotEvent.victimId }

        val xOffset = bullet.x - bot.x
        val yOffset = bullet.y - bot.y

        val explosion = BotHitExplosion(
            bot.x,
            bot.y,
            xOffset,
            yOffset,
            4.0,
            40.0,
            25,
            time
        )
        synchronized(explosions) {
            explosions.add(explosion)
        }
    }

    private fun onBulletHitWall(bulletHitWallEvent: BulletHitWallEvent) {
        val bullet = bulletHitWallEvent.bullet
        val explosion = CircleBurst(bullet.x, bullet.y, 4.0, 40.0, 25, time)
        synchronized(explosions) {
            explosions.add(explosion)
        }
    }

    private fun onBulletHitBullet(bulletHitBulletEvent: BulletHitBulletEvent) {
        val bullet1 = bulletHitBulletEvent.bullet
        val bullet2 = bulletHitBulletEvent.hitBullet

        val x = (bullet1.x + bullet2.x) / 2
        val y = (bullet1.y + bullet2.y) / 2

        val explosion = CircleBurst(x, y, 4.0, 40.0, 25, time)
        synchronized(explosions) {
            explosions.add(explosion)
        }
    }

    private var deltaX = 0
    private var deltaY = 0

    private var pressedMouseX = 0
    private var pressedMouseY = 0

    private fun onMousePressed(e: MouseEvent) {
        pressedMouseX = e.x - deltaX
        pressedMouseY = e.y - deltaY
    }

    private fun onMouseDragged(e: MouseEvent) {
        deltaX = e.x - pressedMouseX
        deltaY = e.y - pressedMouseY
        repaint()
    }

    private fun onMouseWheel(e: MouseWheelEvent) {
        var newScale = scale
        if (e.unitsToScroll > 0) {
            newScale *= 1.2
        } else if (e.unitsToScroll < 0) {
            newScale /= 1.2
        }
        if (newScale != scale && newScale >= 0.10 && newScale <= 10) {
            scale = newScale
            repaint()
        }
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

        val marginX = (size.width - arenaWidth * scale) / 2
        val marginY = (size.height - arenaHeight * scale) / 2

        // Move the offset of the arena
        g.translate(marginX + deltaX, marginY + deltaY)

        g.scale(scale, -scale)
        g.translate(0, -arenaHeight) // y-axis on screen is translated into y-axis of cartesian coordinate system

        drawGround(g)
        drawBots(g)
        drawExplosions(g)
        drawBullets(g)
        drawRoundInfo(g)
        drawDebugGraphics(g)
    }

    private fun drawBots(g: Graphics2D) {
        bots.forEach { bot ->
            Tank(bot).paint(g)
            drawScanArc(g, bot)
            drawEnergy(g, bot)
            drawNameAndVersion(g, bot)
        }
    }

    private fun drawBullets(g: Graphics2D) {
        bullets.forEach { drawBullet(g, it) }
    }

    private fun recalcScale() {
        /*
         * Add 30px to height in both directions so that energy/name labels are (at least partially)
         * visible even if bot is at the edge of the arena.
         */
        val arenaPadding = 60
        val viewWidth = ArenaPanel.width.toDouble()
        val viewHeight = ArenaPanel.height.toDouble()

        scale = if (viewWidth == 0.0 || viewHeight == 0.0) {
            1.0
        } else {
            minOf(viewWidth / arenaWidth, viewHeight / (arenaHeight + arenaPadding))
        }

    }

    private fun clearCanvas(g: Graphics) {
        g.color = Color.DARK_GRAY
        g.fillRect(0, 0, size.width, size.height)
    }

    private fun drawGround(g: Graphics) {
        g.color = Color.BLACK
        g.fillRect(0, 0, arenaWidth, arenaHeight)
    }

    private fun drawExplosions(g: Graphics2D) {
        val list = ArrayList(explosions)
        with(list.iterator()) {
            forEach { explosion ->
                explosion.paint(g, time)
                if (explosion.isFinished()) remove()
            }
        }
    }

    private fun drawBullet(g: Graphics2D, bullet: BulletState) {
        val size = 2 * sqrt(2.5 * bullet.power)
        val bulletColor = fromString(bullet.color ?: ColorConstant.DEFAULT_BULLET_COLOR)
        g.color = visibleDark(bulletColor)
        g.fillCircle(bullet.x, bullet.y, size)
    }

    private fun drawScanArc(g: Graphics2D, bot: BotState) {
        if (bot.isDroid) return // Droids have no radar

        val oldState = Graphics2DState(g)

        val scanColor = fromString(bot.scanColor ?: ColorConstant.DEFAULT_SCAN_COLOR)
        g.color = visibleDark(scanColor)
        g.stroke = BasicStroke(1f)
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)

        val arc = Arc2D.Double()

        var startAngle = 360 - bot.radarDirection
        var angleEx = bot.radarSweep

        if (angleEx < 0) {
            startAngle += angleEx
            angleEx *= -1
        }
        startAngle %= 360

        arc.setArcByCenter(bot.x, bot.y, 1200.0, startAngle, angleEx, Arc2D.PIE)

        if (angleEx >= .5) {
            g.fill(arc)
        } else {
            g.draw(arc)
        }

        oldState.restore(g)
    }

    private fun drawRoundInfo(g: Graphics2D) {
        val oldState = Graphics2DState(g)

        g.scale(1.0, -1.0)
        g.color = Color.YELLOW
        g.drawString("Round $round, Turn: $time", 10, 20 - arenaHeight)

        oldState.restore(g)
    }

    private fun drawEnergy(g: Graphics2D, bot: BotState) {
        val oldState = Graphics2DState(g)

        g.color = Color.WHITE
        val text = "%.1f".format(bot.energy)
        val width = g.fontMetrics.stringWidth(text)

        g.scale(1.0, -1.0)
        g.drawString(text, bot.x.toFloat() - width / 2, (-30 - bot.y).toFloat())

        oldState.restore(g)
    }

    private fun drawNameAndVersion(g: Graphics2D, bot: BotState) {
        val oldState = Graphics2DState(g)

        try {
            Client.getParticipant(bot.id).apply {
                g.scale(1.0, -1.0)
                g.color = Color.WHITE

                // bot info
                "$name $version ($id)".apply {
                    drawText(g, this, bot.x, -bot.y + 36)
                }

                // team info
                if (teamName != null) {
                    "$teamName $teamVersion ($teamId)".apply {
                        drawText(g, this, bot.x, -bot.y + 50)
                    }
                }
            }

        } catch (ignore: NoSuchElementException) {
            // Do nothing
        }

        oldState.restore(g)
    }

    private fun drawText(g: Graphics2D, text: String, x: Double, y: Double) {
        val width = g.fontMetrics.stringWidth(text)

        g.drawString(text, x.toFloat() - width / 2, y.toFloat())
    }

    private fun Graphics2D.fillCircle(x: Double, y: Double, size: Double) {
        this.color = color
        val transform = AffineTransform.getTranslateInstance(x, y)
        transform.scale(size, size)
        fill(circleShape.createTransformedArea(transform))
    }

    private fun visibleDark(color: Color): Color {
        if (color.lightness < 0.2) {
            val hsl = color.hsl
            return HslColor(hsl.hue, hsl.saturation, 0.2f).toColor()
        }
        return color
    }

    private fun drawDebugGraphics(g: Graphics2D) {
        bots.forEach { bot -> drawDebugGraphics(g, bot) }
    }

    private fun drawDebugGraphics(g: Graphics2D, bot: BotState) {
        // Early return if no debug graphics
        val debugGraphics = bot.debugGraphics ?: return

        val oldState = Graphics2DState(g)
        try {
            SvgToGraphicsRender.renderSvgToGraphics(debugGraphics, g)
        } catch (ignore: Exception) {
            // Silently ignore SVG parsing/rendering errors
        } finally {
            oldState.restore(g)
        }
    }

    class BotHitExplosion(
        x: Double,
        y: Double,
        private val xOffset: Double,
        private val yOffset: Double,
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