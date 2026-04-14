package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.server.event.*
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.rules.*
import dev.robocode.tankroyale.server.util.forEachUniquePair
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

// ── Outcome data classes ────────────────────────────────────────────────────────

data class BulletHitBulletOutcome(
    val bullet1: Bullet,
    val bullet2: Bullet,
)

data class BulletHitBotOutcome(
    val bullet: Bullet,
    val damage: Double,
    val energyBonus: Double,
    val shooterParticipantId: ParticipantId,
    val victimParticipantId: ParticipantId,
    val shooterId: BotId,
    val victimId: BotId,
)

data class BulletHitResults(
    val bulletHitBullets: List<BulletHitBulletOutcome>,
    val bulletHitBots: List<BulletHitBotOutcome>,
)

data class BotCollisionOutcome(
    val bot1Id: BotId,
    val bot2Id: BotId,
    val isBot1Ramming: Boolean,
    val isBot2Ramming: Boolean,
    val bot1NewPosition: Point,
    val bot2NewPosition: Point,
    val bot1ParticipantId: ParticipantId,
    val bot2ParticipantId: ParticipantId,
)

data class BotWallHitOutcome(
    val botId: BotId,
    val newX: Double,
    val newY: Double,
    val wallDamage: Double,
    val isFirstHit: Boolean,
)

data class BulletWallHitOutcome(
    val bullet: Bullet,
)

data class BulletHitScoringRecord(
    val shooterParticipantId: ParticipantId,
    val victimParticipantId: ParticipantId,
    val damage: Double,
    val isKilled: Boolean,
)

data class RamScoringRecord(
    val rammerParticipantId: ParticipantId,
    val victimParticipantId: ParticipantId,
    val isKilled: Boolean,
)

data class BulletPhaseResult(
    val hitResults: BulletHitResults,
    val scoringRecords: List<BulletHitScoringRecord>,
)

data class BotCollisionPhaseResult(
    val outcomes: List<BotCollisionOutcome>,
    val scoringRecords: List<RamScoringRecord>,
)

// ── Collision Detector ──────────────────────────────────────────────────────────

/** Collision detector for bots and bullets. */
class CollisionDetector(
    private val setup: GameSetup,
    private val participantIds: Set<ParticipantId>,
    private val random: java.util.Random = java.util.Random()
) {

    // ── Public methods (detect → apply → return outcome) ────────────────────

    fun checkAndHandleBulletHits(
        bullets: MutableSet<Bullet>,
        botsMap: Map<BotId, MutableBot>,
        turn: MutableTurn,
    ): BulletPhaseResult {
        val results = detectBulletHits(bullets, botsMap)
        val scoringRecords = applyBulletHitResults(results, bullets, botsMap, turn)
        return BulletPhaseResult(results, scoringRecords)
    }

    fun checkAndHandleBotCollisions(
        botsMap: Map<BotId, MutableBot>,
        lastRound: MutableRound?,
        turn: MutableTurn,
    ): BotCollisionPhaseResult {
        val outcomes = detectBotCollisions(botsMap, lastRound, turn)
        val scoringRecords = applyBotCollisions(outcomes, botsMap, turn)
        return BotCollisionPhaseResult(outcomes, scoringRecords)
    }

    fun checkAndHandleBotWallCollisions(
        botsMap: Map<BotId, MutableBot>,
        botsCopies: Map<BotId, MutableBot>,
        lastRound: MutableRound?,
        turn: MutableTurn,
    ): List<BotWallHitOutcome> {
        val outcomes = detectBotWallCollisions(botsMap, botsCopies, lastRound)
        applyBotWallCollisions(outcomes, botsMap, turn)
        return outcomes
    }

    fun checkAndHandleBulletWallCollisions(
        bullets: MutableSet<Bullet>,
        turn: MutableTurn,
    ): List<BulletWallHitOutcome> {
        val outcomes = detectBulletWallCollisions(bullets)
        applyBulletWallCollisions(outcomes, bullets, turn)
        return outcomes
    }

    fun constrainBotPositions(botsMap: Map<BotId, MutableBot>, botsCopies: Map<BotId, MutableBot>) {
        botsMap.values.forEach { bot ->
            val (previousX, previousY) = botsCopies[bot.id]!!.position
            val (x, y) = constrainBotPosition(previousX, previousY, bot.x, bot.y)
            bot.x = x
            bot.y = y
        }
    }

    // ── Detection (pure — no mutations to bots, bullets, turn, or scores) ───

    private fun detectBulletHits(
        bullets: Set<Bullet>,
        botsMap: Map<BotId, MutableBot>,
    ): BulletHitResults {
        val bulletBulletOutcomes = mutableListOf<BulletHitBulletOutcome>()
        val bulletBotOutcomes = mutableListOf<BulletHitBotOutcome>()

        val bulletList = bullets.toList()
        if (bulletList.isNotEmpty()) {
            val bulletLines = bulletList.map { BulletLine(it) }

            forEachUniquePair(bulletLines) { bl1, bl2 ->
                if (isColliding(bl1, bl2)) {
                    bulletBulletOutcomes.add(BulletHitBulletOutcome(bl1.bullet, bl2.bullet))
                }
            }

            for (bulletLine in bulletLines) {
                for (bot in botsMap.values) {
                    if (bulletLine.bullet.botId == bot.id) continue
                    if (isBulletHittingBot(bulletLine, bot)) {
                        bulletBotOutcomes.add(computeBulletHitBotOutcome(bulletLine.bullet, bot))
                    }
                }
            }
        }

        return BulletHitResults(bulletBulletOutcomes, bulletBotOutcomes)
    }

    private fun computeBulletHitBotOutcome(bullet: Bullet, bot: MutableBot): BulletHitBotOutcome {
        val shooterId = bullet.botId
        val victimId = bot.id
        return BulletHitBotOutcome(
            bullet = bullet,
            damage = calcBulletDamage(bullet.power),
            energyBonus = BULLET_HIT_ENERGY_GAIN_FACTOR * bullet.power,
            shooterParticipantId = participantIds.first { it.botId == shooterId },
            victimParticipantId = participantIds.first { it.botId == victimId },
            shooterId = shooterId,
            victimId = victimId,
        )
    }

    private fun detectBotCollisions(
        botsMap: Map<BotId, MutableBot>,
        lastRound: MutableRound?,
        turn: MutableTurn,
    ): List<BotCollisionOutcome> {
        val outcomes = mutableListOf<BotCollisionOutcome>()
        val bots = botsMap.values.toList()

        forEachUniquePair(bots) { bot1, bot2 ->
            if (isBotsBoundingCirclesColliding(bot1, bot2)) {
                outcomes.add(computeBotCollisionOutcome(bot1, bot2, lastRound, turn.turnNumber))
            }
        }

        return outcomes
    }

    private fun computeBotCollisionOutcome(
        bot1: MutableBot,
        bot2: MutableBot,
        lastRound: MutableRound?,
        turnNumber: Int,
    ): BotCollisionOutcome {
        val lastTurn = lastRound?.lastTurn
        val (bot1NewPos, bot2NewPos) = if (turnNumber == 1 || lastTurn == null) {
            Pair(bot1.position, randomSafePosition())
        } else {
            val oldPos1 = lastTurn.getBot(bot1.id)!!.position
            val oldPos2 = lastTurn.getBot(bot2.id)!!.position
            Pair(Point(oldPos1.x, oldPos1.y), Point(oldPos2.x, oldPos2.y))
        }

        return BotCollisionOutcome(
            bot1Id = bot1.id,
            bot2Id = bot2.id,
            isBot1Ramming = isRamming(bot1, bot2),
            isBot2Ramming = isRamming(bot2, bot1),
            bot1NewPosition = bot1NewPos,
            bot2NewPosition = bot2NewPos,
            bot1ParticipantId = participantIds.first { it.botId == bot1.id },
            bot2ParticipantId = participantIds.first { it.botId == bot2.id },
        )
    }

    private fun detectBotWallCollisions(
        botsMap: Map<BotId, MutableBot>,
        botsCopies: Map<BotId, MutableBot>,
        lastRound: MutableRound?,
    ): List<BotWallHitOutcome> {
        if (lastRound?.lastTurn == null) return emptyList()

        val outcomes = mutableListOf<BotWallHitOutcome>()
        for (bot in botsMap.values) {
            val (previousX, previousY) = botsCopies[bot.id]!!.position
            val (newX, newY) = constrainBotPosition(previousX, previousY, bot.x, bot.y)
            val hitWall = bot.x != newX || bot.y != newY
            if (hitWall) {
                val isFirstHit =
                    lastRound.lastTurn?.getEvents(bot.id)?.none { it is BotHitWallEvent } != false
                outcomes.add(
                    BotWallHitOutcome(
                        botId = bot.id,
                        newX = newX,
                        newY = newY,
                        wallDamage = if (isFirstHit) calcWallDamage(bot.speed) else 0.0,
                        isFirstHit = isFirstHit,
                    )
                )
            }
        }
        return outcomes
    }

    private fun detectBulletWallCollisions(bullets: Set<Bullet>): List<BulletWallHitOutcome> =
        bullets.filter { isPointOutsideArena(it.position()) }
            .map { BulletWallHitOutcome(it.copy()) }

    // ── Apply (mutation — writes to bots, bullets, turn, and scores) ────────

    private fun applyBulletHitResults(
        results: BulletHitResults,
        bullets: MutableSet<Bullet>,
        botsMap: Map<BotId, MutableBot>,
        turn: MutableTurn,
    ): List<BulletHitScoringRecord> {
        val scoringRecords = mutableListOf<BulletHitScoringRecord>()

        for (outcome in results.bulletHitBullets) {
            val event1 = BulletHitBulletEvent(turn.turnNumber, outcome.bullet1, outcome.bullet2)
            val event2 = BulletHitBulletEvent(turn.turnNumber, outcome.bullet2, outcome.bullet1)
            turn.addPrivateBotEvent(outcome.bullet1.botId, event1)
            turn.addPrivateBotEvent(outcome.bullet2.botId, event2)
            turn.addObserverEvent(event1)
            bullets -= outcome.bullet1
            bullets -= outcome.bullet2
        }

        for (outcome in results.bulletHitBots) {
            val bot = botsMap[outcome.victimId] ?: continue
            val victimEnergyAfterHit = bot.energy - outcome.damage
            val isKilled = bot.isAlive && victimEnergyAfterHit < 0

            val cappedDamage = if (bot.energy < outcome.damage) bot.energy.coerceAtLeast(0.0) else outcome.damage

            bot.applyDamage(outcome.damage)
            botsMap[outcome.shooterId]?.changeEnergy(outcome.energyBonus)

            scoringRecords += BulletHitScoringRecord(
                outcome.shooterParticipantId,
                outcome.victimParticipantId,
                cappedDamage,
                isKilled,
            )

            val event = BulletHitBotEvent(
                turn.turnNumber, outcome.bullet, outcome.victimId, outcome.damage, victimEnergyAfterHit
            )
            turn.addPrivateBotEvent(outcome.bullet.botId, event)
            turn.addPrivateBotEvent(outcome.victimId, event)
            turn.addObserverEvent(event)

            bullets.removeIf { it.id == outcome.bullet.id }
        }

        return scoringRecords
    }

    private fun applyBotCollisions(
        outcomes: List<BotCollisionOutcome>,
        botsMap: Map<BotId, MutableBot>,
        turn: MutableTurn,
    ): List<RamScoringRecord> {
        val scoringRecords = mutableListOf<RamScoringRecord>()

        for (outcome in outcomes) {
            val bot1 = botsMap[outcome.bot1Id] ?: continue
            val bot2 = botsMap[outcome.bot2Id] ?: continue

            // Apply ram damage and determine kills
            val bot1WasAlive = bot1.isAlive
            val bot2WasAlive = bot2.isAlive
            bot1.applyDamage(RAM_DAMAGE)
            bot2.applyDamage(RAM_DAMAGE)
            val bot1Killed = bot1.isDead && bot1WasAlive
            val bot2Killed = bot2.isDead && bot2WasAlive

            // Collect scoring records for ram hits
            if (outcome.isBot1Ramming) {
                scoringRecords += RamScoringRecord(outcome.bot1ParticipantId, outcome.bot2ParticipantId, bot2Killed)
            }
            if (outcome.isBot2Ramming) {
                scoringRecords += RamScoringRecord(outcome.bot2ParticipantId, outcome.bot1ParticipantId, bot1Killed)
            }

            // Set positions
            bot1.position = outcome.bot1NewPosition
            bot2.position = outcome.bot2NewPosition

            // Stop ramming bots
            if (outcome.isBot1Ramming) bot1.speed = 0.0
            if (outcome.isBot2Ramming) bot2.speed = 0.0

            // Create events (using post-mutation state for energy/position)
            val event1 = BotHitBotEvent(
                turn.turnNumber, bot1.id, bot2.id, bot2.energy, bot2.x, bot2.y, outcome.isBot1Ramming
            )
            val event2 = BotHitBotEvent(
                turn.turnNumber, bot2.id, bot1.id, bot1.energy, bot1.x, bot1.y, outcome.isBot2Ramming
            )
            turn.addPrivateBotEvent(bot1.id, event1)
            turn.addPrivateBotEvent(bot2.id, event2)
            turn.addObserverEvent(event1)
            turn.addObserverEvent(event2)
        }

        return scoringRecords
    }

    private fun applyBotWallCollisions(
        outcomes: List<BotWallHitOutcome>,
        botsMap: Map<BotId, MutableBot>,
        turn: MutableTurn,
    ) {
        for (outcome in outcomes) {
            val bot = botsMap[outcome.botId] ?: continue
            bot.x = outcome.newX
            bot.y = outcome.newY
            if (outcome.isFirstHit) {
                val event = BotHitWallEvent(turn.turnNumber, bot.id)
                turn.addPrivateBotEvent(bot.id, event)
                turn.addObserverEvent(event)
                bot.applyDamage(outcome.wallDamage)
            }
            bot.speed = 0.0
        }
    }

    private fun applyBulletWallCollisions(
        outcomes: List<BulletWallHitOutcome>,
        bullets: MutableSet<Bullet>,
        turn: MutableTurn,
    ) {
        val removedBulletIds = outcomes.map { it.bullet.id }.toSet()
        bullets.removeIf { it.id in removedBulletIds }
        for (outcome in outcomes) {
            val event = BulletHitWallEvent(turn.turnNumber, outcome.bullet)
            turn.addPrivateBotEvent(outcome.bullet.botId, event)
            turn.addObserverEvent(event)
        }
    }

    // ── Geometry helpers (unchanged) ────────────────────────────────────────

    private fun randomSafePosition(): Point {
        val x = BOT_BOUNDING_CIRCLE_RADIUS + random.nextDouble() * (setup.arenaWidth - BOT_BOUNDING_CIRCLE_DIAMETER)
        val y = BOT_BOUNDING_CIRCLE_RADIUS + random.nextDouble() * (setup.arenaHeight - BOT_BOUNDING_CIRCLE_DIAMETER)
        return Point(x, y)
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

    private fun isColliding(bulletLine1: BulletLine, bulletLine2: BulletLine): Boolean =
        isBulletsMaxBoundingCirclesColliding(bulletLine1.end, bulletLine2.end) &&
                isLineIntersectingLine(bulletLine1.line, bulletLine2.line)

    private fun isBulletHittingBot(bulletLine: BulletLine, bot: IBot): Boolean =
        isLineIntersectingCircle(bulletLine.line, bot.position, BOT_BOUNDING_CIRCLE_RADIUS)

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
