package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.server.event.*
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.rules.*
import dev.robocode.tankroyale.server.score.ScoreTracker
import dev.robocode.tankroyale.server.score.ScoreCalculator
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pure turn-step pipeline logic.
 * Encapsulates the sequential physics and scoring logic for a single turn.
 */
class TurnProcessor(
    private val setup: GameSetup,
    private val gunEngine: GunEngine,
    private val collisionDetector: CollisionDetector,
    private val scoreTracker: ScoreTracker,
    private val scoreCalculator: ScoreCalculator,
    private val participantIds: Set<ParticipantId>
) {
    /**
     * Data class representing the outcome of a turn.
     */
    data class TurnResult(
        val inactivityCounter: Int,
        val roundOutcome: RoundOutcome?
    )

    /**
     * Data class representing the outcome of a round.
     */
    data class RoundOutcome(
        val gameEnded: Boolean,
        val scores: List<Score>,
        val winnerBotIds: List<BotId>,
    )

    /**
     * Processes a single turn.
     * @param turn current mutable turn to update.
     * @param botsMap current map of bots.
     * @param botIntentsMap current map of bot intents.
     * @param botsCopies copies of bots from the previous turn.
     * @param round current mutable round.
     * @param bullets current set of bullets.
     * @param inactivityCounter current inactivity counter.
     * @return the result of the turn processing.
     */
    fun processTurn(
        turn: MutableTurn,
        botsMap: MutableMap<BotId, MutableBot>,
        botIntentsMap: MutableMap<BotId, BotIntent>,
        botsCopies: MutableMap<BotId, MutableBot>,
        round: MutableRound,
        bullets: MutableSet<Bullet>,
        inactivityCounter: Int
    ): TurnResult {
        var currentInactivityCounter = inactivityCounter

        deepCopyBots(botsMap, botsCopies)

        // ── Physics pipeline (sequential — each step mutates state for the next) ───
        gunEngine.coolDownAndFireGuns(botsMap, botIntentsMap, botsCopies, round, bullets, turn)
        executeBotIntents(botsMap, botIntentsMap, turn)
        collisionDetector.checkAndHandleBotWallCollisions(botsMap, botsCopies, round, turn)

        val botCollisionResult = collisionDetector.checkAndHandleBotCollisions(botsMap, round, turn)
        botCollisionResult.scoringRecords.forEach {
            scoreTracker.registerRamHit(it.rammerParticipantId, it.victimParticipantId, it.isKilled)
        }

        collisionDetector.constrainBotPositions(botsMap, botsCopies)
        checkAndHandleScans(turn, botsMap, botIntentsMap, botsCopies)
        val newBullets = updateBulletPositions(bullets)
        bullets.clear()
        bullets.addAll(newBullets)
        collisionDetector.checkAndHandleBulletWallCollisions(bullets, turn)

        val bulletPhaseResult = collisionDetector.checkAndHandleBulletHits(bullets, botsMap, turn)
        bulletPhaseResult.scoringRecords.forEach {
            scoreTracker.registerBulletHit(it.shooterParticipantId, it.victimParticipantId, it.damage, it.isKilled)
        }

        // ── Post-physics detect → apply (ordered: damage affects subsequent checks) ─
        if (bulletPhaseResult.hitResults.bulletHitBots.isNotEmpty()) currentInactivityCounter = 0

        val inactive = isInactive(currentInactivityCounter)
        currentInactivityCounter = applyInactivity(inactive, currentInactivityCounter, botsMap)

        val disabledBotIds = detectDisabledBotIds(botsMap)
        applyDisabledBots(disabledBotIds, botIntentsMap)

        val defeatedParticipants = detectDefeatedParticipants(botsMap)
        applyDefeatedBots(defeatedParticipants, turn)

        val roundOutcome = computeRoundOutcome(round, botsMap, bullets)

        // ── Snapshot + terminal state ──────────────────────────────────────────────
        turn.copyBots(botsMap.values)
        turn.copyBullets(bullets)
        botsMap.values.removeIf(IBot::isDead)

        return TurnResult(currentInactivityCounter, roundOutcome)
    }

    private fun deepCopyBots(botsMap: Map<BotId, MutableBot>, botsCopies: MutableMap<BotId, MutableBot>) {
        botsMap.forEach {
            botsCopies[it.key] = deepCopy(it.value)
        }
    }

    private fun deepCopy(bot: MutableBot) = MutableBot(
        id = bot.id,
        isDroid = bot.isDroid,
        position = Point(bot.x, bot.y),
        direction = bot.direction,
        gunDirection = bot.gunDirection,
        radarDirection = bot.radarDirection,
    )

    /** Execute bot intents for all bots that are not disabled */
    private fun executeBotIntents(
        botsMap: Map<BotId, MutableBot>,
        botIntentsMap: Map<BotId, BotIntent>,
        turn: MutableTurn
    ) {
        botsMap.values.forEach { bot ->
            if (bot.isEnabled) executeBotIntent(bot, botIntentsMap, turn)
        }
    }

    /**
     * Executes the bot states intent.
     * @param bot is the bot top execute the bot intent for.
     */
    private fun executeBotIntent(bot: MutableBot, botIntentsMap: Map<BotId, BotIntent>, turn: MutableTurn) {
        botIntentsMap[bot.id]?.apply {
            bot.speed = calcNewBotSpeed(bot.speed, targetSpeed ?: 0.0)
            bot.moveToNewPosition()

            updateBotTurnRatesAndDirections(bot, this)
            updateBotColors(bot, this)
            updateDebugGraphics(bot, this)

            // Transfer one-shot std streams from intent to bot, then clear
            bot.stdOut = stdOut
            bot.stdErr = stdErr
            stdOut = null
            stdErr = null

            processTeamMessages(bot, this, turn)
        }
    }

    /** Updates bullet positions */
    private fun updateBulletPositions(bullets: Set<Bullet>): Set<Bullet> {
        return bullets.map { it.copy(tick = it.tick + 1) }.toSet()
    }

    /** Pure: checks whether bots have been collectively inactive long enough to take damage. */
    private fun isInactive(inactivityCounter: Int): Boolean = inactivityCounter > setup.maxInactivityTurns

    /** Apply: increments inactivity counter and, if inactive, applies damage to all bots. */
    private fun applyInactivity(inactive: Boolean, inactivityCounter: Int, botsMap: Map<BotId, MutableBot>): Int {
        val newInactivityCounter = inactivityCounter + 1
        if (inactive) {
            botsMap.values.forEach { it.applyDamage(INACTIVITY_DAMAGE) }
        }
        return newInactivityCounter
    }

    /** Pure: returns the IDs of bots that are currently disabled (energy ≈ 0). */
    private fun detectDisabledBotIds(botsMap: Map<BotId, MutableBot>): Set<BotId> =
        botsMap.values.filter { it.isDisabled }.map { it.id }.toSet()

    /** Apply: disables movement intents for the given bot IDs. */
    private fun applyDisabledBots(disabledBotIds: Set<BotId>, botIntentsMap: Map<BotId, BotIntent>) {
        disabledBotIds.forEach { botId -> botIntentsMap[botId]?.disableMovement() }
    }

    /** Pure: returns participant IDs of bots that have been defeated (isDead). */
    private fun detectDefeatedParticipants(botsMap: Map<BotId, MutableBot>): Set<ParticipantId> =
        botsMap.values.filter { it.isDead }
            .map { bot -> participantIds.first { it.botId == bot.id } }
            .toSet()

    /** Apply: emits death events and registers deaths for scoring. */
    private fun applyDefeatedBots(deadParticipantIds: Set<ParticipantId>, turn: MutableTurn) {
        deadParticipantIds.forEach {
            val botDeathEvent = BotDeathEvent(turn.turnNumber, it.botId)
            turn.addPublicBotEvent(botDeathEvent)
            turn.addObserverEvent(botDeathEvent)
        }
        scoreTracker.registerDeaths(deadParticipantIds)
    }

    /** Checks the scan field for scanned bots. */
    private fun checkAndHandleScans(
        turn: MutableTurn,
        botsMap: Map<BotId, MutableBot>,
        botIntentsMap: Map<BotId, BotIntent>,
        botsCopies: Map<BotId, MutableBot>
    ) {
        val bots = botsMap.values.toList()
        for (i in bots.indices) {
            val scanningBot = bots[i]

            if (scanningBot.isDroid) continue // droids cannot use scanning

            val (startAngle, endAngle) = getScanAngles(scanningBot)

            for (j in bots.indices) {
                if (i != j) {
                    val botBeingScanned = bots[j]
                    if (isBotScanned(scanningBot, botBeingScanned, startAngle, endAngle, botsMap, botIntentsMap, botsCopies)) {
                        handleScannedBot(turn, scanningBot, botBeingScanned)
                    }
                }
            }
        }
    }

    /**
     * Handle scanned bot.
     * @param scanningBot the bot scanning an opponent bot.
     * @param botBeingScanned the bot being scanned.
     */
    private fun handleScannedBot(turn: MutableTurn, scanningBot: MutableBot, botBeingScanned: IBot) {
        createAndAddScannedBotEventToTurn(turn, scanningBot.id, botBeingScanned)
    }

    /**
     * Checks if a bot is scanning another bot.
     * @param scanningBot is the bot performing the scanning.
     * @param scannedBot is the bot exposed for scanning.
     * @param scanStartAngle is the start angle of the scan arc.
     * @param scanEndAngle is the end angle of the scan arc.
     * @return `true` if the scannedBot was scanned; `false` otherwise.
     */
    private fun isBotScanned(
        scanningBot: IBot,
        scannedBot: IBot,
        scanStartAngle: Double,
        scanEndAngle: Double,
        botsMap: Map<BotId, MutableBot>,
        botIntentsMap: Map<BotId, BotIntent>,
        botsCopies: Map<BotId, MutableBot>
    ) = isScanningOrMoving(scanningBot.id, botsMap, botIntentsMap, botsCopies) &&
            if (scanStartAngle == scanEndAngle) {
                // For a single angle, the scan is a line segment from the scanner out to the radar radius.
                val endX = scanningBot.x + cos(Math.toRadians(scanStartAngle)) * RADAR_RADIUS
                val endY = scanningBot.y + sin(Math.toRadians(scanStartAngle)) * RADAR_RADIUS
                isLineIntersectingCircle(
                    Line(scanningBot.x, scanningBot.y, endX, endY),
                    scannedBot.position,
                    BOT_BOUNDING_CIRCLE_RADIUS
                )
            } else {
                isCircleIntersectingCircleSector(
                    scannedBot.position, BOT_BOUNDING_CIRCLE_RADIUS,
                    scanningBot.position, RADAR_RADIUS,
                    scanStartAngle, scanEndAngle
                )
            }

    /**
     * Checks if a bot is scanning, meaning that it must be either rescanning or moving.
     * @param botId is the id of the bot.
     * @return `true` if the bot is scanning; `false` otherwise.
     */
    private fun isScanningOrMoving(
        botId: BotId,
        botsMap: Map<BotId, MutableBot>,
        botIntentsMap: Map<BotId, BotIntent>,
        botsCopies: Map<BotId, MutableBot>
    ): Boolean {
        return isRescanning(botId, botIntentsMap) || isMoving(botId, botsMap, botsCopies)
    }

    /**
     * Checks if a bot is rescanning.
     * @param botId is the id of the bot.
     * @return `true` if the bot is scanning; `false` otherwise.
     */
    private fun isRescanning(botId: BotId, botIntentsMap: Map<BotId, BotIntent>): Boolean {
        return botIntentsMap[botId]?.rescan ?: false
    }

    /**
     * Checks if a bot is moving, meaning that the x,y position or a direction has changed.
     * @param botId is the id of the bot.
     * @return `true` if the bot is moving; `false` otherwise.
     */
    private fun isMoving(botId: BotId, botsMap: Map<BotId, MutableBot>, botsCopies: Map<BotId, MutableBot>): Boolean {
        val currentState = botsMap[botId]!!
        val previousState = botsCopies[botId]!!

        return currentState.x != previousState.x
                || currentState.y != previousState.y
                || currentState.direction != previousState.direction
                || currentState.gunDirection != previousState.gunDirection
                || currentState.radarDirection != previousState.radarDirection
    }

    /**
     * Creates and adds scanned-bot-events to the turn.
     * @param scanningBotId is the id of the bot performing the scanning.
     * @param scannedBot is the bot exposed for scanning.
     */
    private fun createAndAddScannedBotEventToTurn(turn: MutableTurn, scanningBotId: BotId, scannedBot: IBot) {
        val scannedBotEvent = ScannedBotEvent(
            turn.turnNumber,
            scanningBotId,
            scannedBot.id,
            scannedBot.energy,
            scannedBot.x,
            scannedBot.y,
            scannedBot.direction,
            scannedBot.speed
        )
        turn.addPrivateBotEvent(scanningBotId, scannedBotEvent)
        turn.addObserverEvent(scannedBotEvent)
    }

    /**
     * Returns the scan angles for a bot.
     * @param bot is the bot.
     * @return a pair of doubles, where the first double is the start angle, and the second double is the end angle.
     */
    private fun getScanAngles(bot: IBot): Pair<Double, Double> {
        val spreadAngle = bot.radarSpreadAngle
        val absSpreadAngle = abs(spreadAngle)

        // Always use the radar direction as the reference point
        val radarDirection = bot.radarDirection

        // Calculate start and end based on the sign of the spread angle
        val (startAngle, endAngle) = if (spreadAngle >= 0) {
            Pair(
                normalizeAbsoluteDegrees(radarDirection - absSpreadAngle),
                radarDirection
            )
        } else {
            Pair(
                radarDirection,
                normalizeAbsoluteDegrees(radarDirection + absSpreadAngle)
            )
        }

        return Pair(startAngle, endAngle)
    }

    /**
     * Returns the outcome of the round if the round is over; `null` if still in progress.
     * Pure: reads state only — no mutations to [round], [gameState], or [accumulatedScoreCalculator].
     */
    private fun computeRoundOutcome(round: MutableRound, botsMap: Map<BotId, MutableBot>, bullets: Set<Bullet>): RoundOutcome? {
        if (!isRoundOver(botsMap, bullets)) return null
        val scores = scoreCalculator.getScores()
        val winnerBotIds = scores.filter { it.rank == 1 }.map { it.participantId.botId }
        return RoundOutcome(
            gameEnded = round.roundNumber >= setup.numberOfRounds,
            scores = scores,
            winnerBotIds = winnerBotIds,
        )
    }

    private fun isRoundOver(botsMap: Map<BotId, MutableBot>, bullets: Set<Bullet>) = run {
        // distinctBy(id) is necessary to take account for both bots and teams
        val aliveBotsOrTeams = getBotsOrTeams(botsMap, MutableBot::isAlive).distinctBy { it.id }
        val aliveCount = aliveBotsOrTeams.count()
        if (aliveCount <= 1) {
            true
        } else {
            if (bullets.size > 0) {
                false
            } else {
                // When no bullets or functioning bots remain, the round ends immediately as a draw to speed things up
                val disabledCount = getBotsOrTeams(botsMap, MutableBot::isDisabled).distinctBy { it.id }.count()
                disabledCount == aliveCount
            }
        }
    }

    private fun getBotsOrTeams(botsMap: Map<BotId, MutableBot>, filter: (MutableBot) -> Boolean): Collection<ParticipantId> {
        val botIds = botsMap.values.filter { filter.invoke(it) }.map { it.id }

        return participantIds.filter { botIds.contains(it.botId) }.distinct()
    }

    private fun processTeamMessages(bot: MutableBot, intent: BotIntent, turn: MutableTurn) {
        val teamMessages = intent.teamMessages ?: return
        for (index in 0 until teamMessages.size.coerceAtMost(MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN)) {
            val msg = teamMessages[index]
            if (msg.message.length > MAX_TEAM_MESSAGE_SIZE) continue
            if (msg.receiverId != null) {
                turn.addPrivateBotEvent(
                    msg.receiverId, TeamMessageEvent(turn.turnNumber, msg.message, msg.messageType, bot.id)
                )
            } else {
                bot.teammateIds.forEach { teammateId ->
                    turn.addPrivateBotEvent(
                        teammateId, TeamMessageEvent(turn.turnNumber, msg.message, msg.messageType, bot.id)
                    )
                }
            }
        }
        intent.teamMessages = null
    }

    private fun updateBotTurnRatesAndDirections(bot: MutableBot, intent: BotIntent) {
        val turnRate = limitTurnRate(intent.turnRate ?: 0.0, bot.speed)
        val gunTurnRate = limitGunTurnRate(intent.gunTurnRate ?: 0.0)
        val radarTurnRate = limitRadarTurnRate(intent.radarTurnRate ?: 0.0)

        bot.turnRate = turnRate
        bot.gunTurnRate = gunTurnRate
        bot.radarTurnRate = radarTurnRate

        // -- Gun adjustment
        var gunAdjustment = turnRate + gunTurnRate
        if (intent.adjustGunForBodyTurn == true) {
            gunAdjustment -= turnRate
        }

        // -- Radar adjustment
        var radarAdjustment = gunAdjustment + radarTurnRate
        if (intent.adjustRadarForGunTurn == true) {
            radarAdjustment -= gunTurnRate

            if (intent.adjustGunForBodyTurn == true) {  // orig. Robocode compatibility
                radarAdjustment += turnRate
            }
        }
        if (intent.adjustRadarForBodyTurn == true) {
            radarAdjustment -= turnRate
        }

        bot.direction = normalizeAbsoluteDegrees(bot.direction + turnRate)
        bot.gunDirection = normalizeAbsoluteDegrees(bot.gunDirection + gunAdjustment)
        bot.radarDirection = normalizeAbsoluteDegrees(bot.radarDirection + radarAdjustment)
        bot.radarSpreadAngle = radarAdjustment
    }

    private fun updateBotColors(bot: MutableBot, intent: BotIntent) {
        bot.apply {
            bodyColor = fromColor(intent.bodyColor)
            turretColor = fromColor(intent.turretColor)
            radarColor = fromColor(intent.radarColor)
            bulletColor = fromColor(intent.bulletColor)
            scanColor = fromColor(intent.scanColor)
            tracksColor = fromColor(intent.tracksColor)
            gunColor = fromColor(intent.gunColor)
        }
    }

    private fun updateDebugGraphics(bot: MutableBot, intent: BotIntent) {
        bot.debugGraphics = intent.debugGraphics
    }

    private fun fromColor(color: String?) = color?.let { Color.from(it) }
}
