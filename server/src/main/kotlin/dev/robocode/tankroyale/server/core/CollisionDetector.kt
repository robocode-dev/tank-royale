package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.server.event.*
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.rules.*
import dev.robocode.tankroyale.server.score.ScoreTracker
import java.lang.Math.toDegrees
import kotlin.math.abs
import kotlin.math.atan2

/** Maximum bounding circle diameter of a bullet moving with max speed */
private val bulletMaxBoundingCircleDiameter: Double = 2 * MAX_BULLET_SPEED

/** Square of maximum bounding circle diameter of a bullet moving with max speed */
private val bulletMaxBoundingCircleDiameterSquared: Double =
    bulletMaxBoundingCircleDiameter * bulletMaxBoundingCircleDiameter

/** Square of the bounding circle diameter of a bot */
private const val BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED: Double =
    BOT_BOUNDING_CIRCLE_DIAMETER.toDouble() * BOT_BOUNDING_CIRCLE_DIAMETER

/** Collision detector for bots and bullets. */
class CollisionDetector(
    private val setup: GameSetup,
    private val participantIds: Set<ParticipantId>,
    private val scoreTracker: ScoreTracker
) {

    fun checkAndHandleBulletHits(
        bullets: MutableSet<Bullet>,
        botsMap: Map<BotId, MutableBot>,
        turn: MutableTurn,
        onInactivityReset: () -> Unit
    ) {
        val bulletList = bullets.toList()
        val bulletCount = bulletList.size
        if (bulletCount > 0) {
            val bulletLines = bulletList.map { BulletLine(it) }

            for (i in 0 until bulletCount) {
                for (j in i + 1 until bulletCount) {
                    if (isColliding(bulletLines[i], bulletLines[j])) {
                        handleBulletHitBullet(bulletLines[i].bullet, bulletLines[j].bullet, bullets, turn)
                    }
                }
                checkAndHandleBulletHitBot(bulletLines[i], bullets, botsMap, turn, onInactivityReset)
            }
        }
    }

    private fun handleBulletHitBullet(
        bullet1: Bullet,
        bullet2: Bullet,
        bullets: MutableSet<Bullet>,
        turn: MutableTurn
    ) {
        val event1 = BulletHitBulletEvent(turn.turnNumber, bullet1, bullet2)
        val event2 = BulletHitBulletEvent(turn.turnNumber, bullet2, bullet1)

        turn.apply {
            addPrivateBotEvent(bullet1.botId, event1)
            addPrivateBotEvent(bullet2.botId, event2)
            addObserverEvent(event1)
        }
        bullets -= bullet1
        bullets -= bullet2
    }

    private fun isColliding(bulletLine1: BulletLine, bulletLine2: BulletLine): Boolean =
        isBulletsMaxBoundingCirclesColliding(bulletLine1.end, bulletLine2.end) &&
                isLineIntersectingLine(bulletLine1.line, bulletLine2.line)

    private fun checkAndHandleBulletHitBot(
        bulletLine: BulletLine,
        bullets: MutableSet<Bullet>,
        botsMap: Map<BotId, MutableBot>,
        turn: MutableTurn,
        onInactivityReset: () -> Unit
    ) {
        for (bot in botsMap.values) {
            if (bulletLine.bullet.botId == bot.id) {
                continue
            }
            if (isBulletHittingBot(bulletLine, bot)) {
                onInactivityReset()
                handleBulletHittingBot(bulletLine.bullet, bot, botsMap, turn)
                bullets.removeIf { bullet -> bullet.id == bulletLine.bullet.id }
            }
        }
    }

    private fun isBulletHittingBot(bulletLine: BulletLine, bot: IBot): Boolean =
        isLineIntersectingCircle(bulletLine.line, bot.position, BOT_BOUNDING_CIRCLE_RADIUS)

    private fun handleBulletHittingBot(
        bullet: Bullet,
        bot: MutableBot,
        botsMap: Map<BotId, MutableBot>,
        turn: MutableTurn
    ) {
        val botId = bullet.botId
        val teamOrBotId = participantIds.first { it.botId == botId }
        val victimId = bot.id
        val victimTeamOrBotId = participantIds.first { it.botId == victimId }

        val damage = calcBulletDamage(bullet.power)
        val isKilled = bot.addDamage(damage)

        val energyBonus = BULLET_HIT_ENERGY_GAIN_FACTOR * bullet.power
        botsMap[botId]?.changeEnergy(energyBonus)

        scoreTracker.registerBulletHit(
            teamOrBotId,
            victimTeamOrBotId,
            damage,
            isKilled
        )

        val bulletHitBotEvent = BulletHitBotEvent(turn.turnNumber, bullet, victimId, damage, bot.energy)
        turn.apply {
            addPrivateBotEvent(bulletHitBotEvent.bullet.botId, bulletHitBotEvent)
            addPrivateBotEvent(bulletHitBotEvent.victimId, bulletHitBotEvent)
            addObserverEvent(bulletHitBotEvent)
        }
    }

    fun checkAndHandleBotCollisions(botsMap: Map<BotId, MutableBot>, lastRound: MutableRound?, turn: MutableTurn) {
        val bots = botsMap.values.toList()
        for (i in bots.indices) {
            for (j in i + 1 until bots.size) {
                if (isBotsBoundingCirclesColliding(bots[i], bots[j])) {
                    handleBotHitBot(bots[i], bots[j], lastRound, turn)
                }
            }
        }
    }

    private fun handleBotHitBot(bot1: MutableBot, bot2: MutableBot, lastRound: MutableRound?, turn: MutableTurn) {
        val isBot1RammingBot2 = isRamming(bot1, bot2)
        val isBot2RammingBot1 = isRamming(bot2, bot1)

        registerRamHit(bot1, bot2, isBot1RammingBot2, isBot2RammingBot1)

        val lastTurn = lastRound?.lastTurn
        if (turn.turnNumber == 1 || lastTurn == null) {
            val x = BOT_BOUNDING_CIRCLE_RADIUS + Math.random() * (setup.arenaWidth - BOT_BOUNDING_CIRCLE_DIAMETER)
            val y = BOT_BOUNDING_CIRCLE_RADIUS + Math.random() * (setup.arenaHeight - BOT_BOUNDING_CIRCLE_DIAMETER)
            bot2.position = Point(x, y)
        } else {
            val oldPos1 = lastTurn.getBot(bot1.id)!!.position
            val oldPos2 = lastTurn.getBot(bot2.id)!!.position
            bot1.position = Point(oldPos1.x, oldPos1.y)
            bot2.position = Point(oldPos2.x, oldPos2.y)
        }

        if (isBot1RammingBot2) bot1.speed = 0.0
        if (isBot2RammingBot1) bot2.speed = 0.0

        val event1 = BotHitBotEvent(turn.turnNumber, bot1.id, bot2.id, bot2.energy, bot2.x, bot2.y, isBot1RammingBot2)
        val event2 = BotHitBotEvent(turn.turnNumber, bot2.id, bot1.id, bot1.energy, bot1.x, bot1.y, isBot2RammingBot1)
        turn.apply {
            addPrivateBotEvent(bot1.id, event1)
            addPrivateBotEvent(bot2.id, event2)
            addObserverEvent(event1)
            addObserverEvent(event2)
        }
    }

    private fun registerRamHit(
        bot1: MutableBot,
        bot2: MutableBot,
        isBot1RammingBot2: Boolean,
        isBot2RammingBot1: Boolean
    ) {
        val bot1Killed = bot1.addDamage(RAM_DAMAGE)
        val bot2Killed = bot2.addDamage(RAM_DAMAGE)
        if (isBot1RammingBot2) {
            scoreTracker.registerRamHit(
                participantIds.first { it.botId == bot1.id },
                participantIds.first { it.botId == bot2.id },
                bot2Killed
            )
        }
        if (isBot2RammingBot1) {
            scoreTracker.registerRamHit(
                participantIds.first { it.botId == bot2.id },
                participantIds.first { it.botId == bot1.id },
                bot1Killed
            )
        }
    }

    fun checkAndHandleBotWallCollisions(
        botsMap: Map<BotId, MutableBot>,
        botsCopies: Map<BotId, MutableBot>,
        lastRound: MutableRound?,
        turn: MutableTurn
    ) {
        for (bot in botsMap.values) {
            val hitWall = adjustBotCoordinatesIfHitWall(bot, botsCopies, lastRound)
            if (hitWall) {
                if (lastRound?.lastTurn?.getEvents(bot.id)?.none { event -> event is BotHitWallEvent } != false) {
                    val botHitWallEvent = BotHitWallEvent(turn.turnNumber, bot.id)
                    turn.addPrivateBotEvent(bot.id, botHitWallEvent)
                    turn.addObserverEvent(botHitWallEvent)

                    bot.addDamage(calcWallDamage(bot.speed))
                }
                bot.speed = 0.0
            }
        }
    }

    private fun adjustBotCoordinatesIfHitWall(
        bot: MutableBot,
        botsCopies: Map<BotId, MutableBot>,
        lastRound: MutableRound?
    ): Boolean {
        var hitWall = false
        if (lastRound?.lastTurn != null) {
            val (previousX, previousY) = botsCopies[bot.id]!!.position
            val (x, y) = constrainBotPosition(previousX, previousY, bot.x, bot.y)
            hitWall = bot.x != x || bot.y != y
            if (hitWall) {
                bot.x = x
                bot.y = y
            }
        }
        return hitWall
    }

    fun constrainBotPositions(botsMap: Map<BotId, MutableBot>, botsCopies: Map<BotId, MutableBot>) {
        botsMap.values.forEach { bot ->
            val (previousX, previousY) = botsCopies[bot.id]!!.position
            val (x, y) = constrainBotPosition(previousX, previousY, bot.x, bot.y)
            bot.x = x
            bot.y = y
        }
    }

    private fun constrainBotPosition(oldX: Double, oldY: Double, x: Double, y: Double): Pair<Double, Double> {
        var newX = x
        var newY = y

        if (x - BOT_BOUNDING_CIRCLE_RADIUS < 0) {
            newX = BOT_BOUNDING_CIRCLE_RADIUS
            val dx = x - oldX
            if (dx != .0) {
                val dy = y - oldY
                val dx2 = newX - oldX
                val dy2 = dy * dx2 / dx
                newY = oldY + dy2
            }
        } else if (x + BOT_BOUNDING_CIRCLE_RADIUS > setup.arenaWidth) {
            newX = setup.arenaWidth - BOT_BOUNDING_CIRCLE_RADIUS
            val dx = x - oldX
            if (dx != .0) {
                val dy = y - oldY
                val dx2 = newX - oldX
                val dy2 = dy * dx2 / dx
                newY = oldY + dy2
            }
        }

        if (y - BOT_BOUNDING_CIRCLE_RADIUS < 0) {
            newY = BOT_BOUNDING_CIRCLE_RADIUS
            val dy = y - oldY
            if (dy != .0) {
                val dx = x - oldX
                val dy2 = newY - oldY
                val dx2 = dx * dy2 / dy
                newX = oldX + dx2
            }
        } else if (y + BOT_BOUNDING_CIRCLE_RADIUS > setup.arenaHeight) {
            newY = setup.arenaHeight - BOT_BOUNDING_CIRCLE_RADIUS
            val dy = y - oldY
            if (dy != .0) {
                val dx = x - oldX
                val dy2 = newY - oldY
                val dx2 = dx * dy2 / dy
                newX = oldX + dx2
            }
        }

        return Pair(newX, newY)
    }

    fun checkAndHandleBulletWallCollisions(bullets: MutableSet<Bullet>, turn: MutableTurn) {
        val iterator = bullets.iterator()
        while (iterator.hasNext()) {
            val bullet = iterator.next()
            if (isPointOutsideArena(bullet.position())) {
                iterator.remove()
                val bulletHitWallEvent = BulletHitWallEvent(turn.turnNumber, bullet.copy())
                turn.addPrivateBotEvent(bullet.botId, bulletHitWallEvent)
                turn.addObserverEvent(bulletHitWallEvent)
            }
        }
    }

    private fun isPointOutsideArena(point: Point): Boolean {
        return point.x <= 0 ||
                point.y <= 0 ||
                point.x >= setup.arenaWidth ||
                point.y >= setup.arenaHeight
    }

    private fun isBulletsMaxBoundingCirclesColliding(pos1: Point, pos2: Point): Boolean {
        val dx = pos2.x - pos1.x
        if (abs(dx) > bulletMaxBoundingCircleDiameter) {
            return false
        }
        val dy = pos2.y - pos1.y
        return abs(dy) <= bulletMaxBoundingCircleDiameter &&
                ((dx * dx) + (dy * dy) <= bulletMaxBoundingCircleDiameterSquared)
    }

    private fun isBotsBoundingCirclesColliding(bot1: IBot, bot2: IBot): Boolean {
        val dx = bot2.x - bot1.x
        if (abs(dx) > BOT_BOUNDING_CIRCLE_DIAMETER) {
            return false
        }
        val dy = bot2.y - bot1.y
        return abs(dy) <= BOT_BOUNDING_CIRCLE_DIAMETER &&
                ((dx * dx) + (dy * dy) <= BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED)
    }

    private fun isRamming(bot: IBot, victim: IBot): Boolean {
        val dx = victim.x - bot.x
        val dy = victim.y - bot.y
        val angle = atan2(dy, dx)
        val bearing = normalizeRelativeDegrees(toDegrees(angle) - bot.direction)
        return (((bot.speed > 0 && (bearing > -90 && bearing < 90))
                || (bot.speed < 0 && (bearing < -90 || bearing > 90))))
    }
}
