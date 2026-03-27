package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.server.model.InitialPosition
import dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.score.AccumulatedScoreCalculator
import dev.robocode.tankroyale.server.score.ScoreCalculator
import dev.robocode.tankroyale.server.event.*
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.model.Color.Companion.from
import dev.robocode.tankroyale.server.rules.*
import dev.robocode.tankroyale.server.score.ScoreTracker
import kotlin.math.abs

/** Square of the bounding circle diameter of a bot */
private const val BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED: Double =
    BOT_BOUNDING_CIRCLE_DIAMETER.toDouble() * BOT_BOUNDING_CIRCLE_DIAMETER


/** Model updater, which is used for keeping track of the model state for each turn and round of a game. */
class ModelUpdater(
    /** Game setup */
    private val setup: GameSetup,
    /** Participant ids */
    private val participantIds: Set<ParticipantId>,
    /** Initial positions */
    private val initialPositions: Map<BotId, InitialPosition>,
    /** Droid flags */
    private val droidFlags: Map<BotId, Boolean /* isDroid */>,
) {
    /** Score tracking */
    private val scoreTracker = ScoreTracker(participantIds)
    private val scoreCalculator = ScoreCalculator(participantIds, scoreTracker)
    private val accumulatedScoreCalculator = AccumulatedScoreCalculator()

    /** Map over all bots */
    private val botsMap = mutableMapOf<BotId, MutableBot>()

    /** Map over copied bots from previous turn */
    private val botsCopies = mutableMapOf<BotId, MutableBot>()

    /** Map over all bot intents */
    private val botIntentsMap = mutableMapOf<BotId, BotIntent>()

    /** Bullets */
    private var bullets = mutableSetOf<Bullet>()

    /** Game state */
    private var gameState = GameState(Arena(setup.arenaWidth, setup.arenaHeight))

    /** Round record */
    private var round = MutableRound(0)

    /** Turn record */
    internal val turn = MutableTurn(0)

    /** Counter to track the number of rounds played (memory leak fix) */
    private var roundCounter = 0

    /** Inactivity counter */
    private var inactivityCounter = 0

    /** Components */
    private val collisionDetector = CollisionDetector(setup, participantIds, scoreTracker)
    private val botInitializer = BotInitializer(setup, participantIds, initialPositions, droidFlags)
    private val gunEngine = GunEngine(setup)

    /** The accumulated results ordered with higher total scores first */
    internal fun getResults() = accumulatedScoreCalculator.getScores()

    /** The number of rounds played so far */
    internal val numberOfRounds: Int get() = roundCounter

    internal fun isAlive(botId: BotId) = botsMap[botId]?.isAlive ?: false

    /**
     * Returns a bot by its id.
     * @param id is the bot id.
     * @return a bot by its id.
     */
    internal fun getBot(id: BotId): MutableBot? = botsMap[id]

    /**
     * Sets the debug enabled flag for a bot.
     * @param id is the bot id.
     * @param enabled is the debug enabled flag.
     */
    internal fun setDebugEnabled(id: BotId, enabled: Boolean) {
        botsMap[id]?.isDebuggingEnabled = enabled
    }

    /**
     * Returns the initial positions of all bots.
     * @return a map of bot id to initial position.
     */
    internal fun getBotInitialPositions(): Map<BotId, Point> =
        botsMap.mapValues { (_, bot) -> Point(bot.x, bot.y) }

    /**
     * Updates game state.
     * @param botIntents is the bot intents, which gives instructions to the game from the individual bots.
     * @return new game state when the game state has been updated.
     */
    fun update(botIntents: Map<BotId, IBotIntent>): GameState {
        updateBotIntents(botIntents)
        if (round.roundEnded || (round.roundNumber == 0 && turn.turnNumber == 0)) {
            nextRound()
        }
        nextTurn()
        return updateGameState()
    }

    /**
     * Updates the current bot intents with the new bot intents.
     * @param botIntents is a map of new bot intents.
     */
    private fun updateBotIntents(botIntents: Map<BotId, IBotIntent>) {
        for ((botId, updateIntent) in botIntents.entries) {
            (botIntentsMap[botId] ?: BotIntent()).apply {
                update(updateIntent)
                botIntentsMap[botId] = this
            }
        }
    }

    /** Proceed with the next round. */
    private fun nextRound() {
        round = round.copy(roundNumber = round.roundNumber).apply {
            roundEnded = false
            roundNumber++
            turns.clear() // Memory leak fix: Clear turns from previous round
        }
        // Initialize to 0; nextTurn() will increment it to 1 before the first TickEvent is mapped/sent
        turn.turnNumber = 0

        // Increment round counter for tracking (memory leak fix)
        roundCounter++

        gunEngine.reset()
        botIntentsMap.clear()
        botsCopies.clear()
        bullets.clear()
        botsMap.clear()
        scoreTracker.clear()
        inactivityCounter = 0

        // Memory leak fix: Clear old rounds from gameState when starting a new round
        // We only need the current round in memory
        gameState.rounds.clear()

        botInitializer.initializeBotStates(botsMap, turn)
    }

    /** Proceed with the next turn. */
    private fun nextTurn() {
        // Increment at the very start of a turn; the first turn becomes 1
        turn.turnNumber++
        turn.resetEvents()

        deepCopyBots()

        gunEngine.coolDownAndFireGuns(botsMap, botIntentsMap, botsCopies, round, bullets, turn)

        executeBotIntents()

        collisionDetector.checkAndHandleBotWallCollisions(botsMap, botsCopies, round, turn)
        collisionDetector.checkAndHandleBotCollisions(botsMap, round, turn)
        collisionDetector.constrainBotPositions(botsMap, botsCopies)

        checkAndHandleScans()

        updateBulletPositions()
        collisionDetector.checkAndHandleBulletWallCollisions(bullets, turn)
        collisionDetector.checkAndHandleBulletHits(bullets, botsMap, turn) { inactivityCounter = 0 }

        checkAndHandleInactivity()
        checkForAndHandleDisabledBots()
        checkAndHandleDefeatedBots()

        checkAndHandleRoundOrGameOver()

        // Store bot and bullet snapshots
        turn.copyBots(botsMap.values)
        turn.copyBullets(bullets)

        // Remove dead bots
        botsMap.values.removeIf(IBot::isDead)
    }

    private fun deepCopyBots() {
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

    /**
     * Updates the game state.
     * @return new game state.
     */
    private fun updateGameState(): GameState {
        round.turns += turn.toTurn()

        // Memory leak fix: Keep only the last 2 turns (current + previous for collision detection)
        // Remove older turns to prevent unbounded memory growth
        if (round.turns.size > 2) {
            round.turns.removeAt(0)
        }

        if (gameState.rounds.size == 0 || gameState.rounds.last().roundNumber != round.roundNumber) {
            gameState.rounds += round
        }
        return gameState
    }

    /** Execute bot intents for all bots that are not disabled */
    private fun executeBotIntents() {
        botsMap.values.forEach { bot ->
            if (bot.isEnabled) executeBotIntent(bot)
        }
    }

    /**
     * Executes the bot states intent.
     * @param bot is the bot top execute the bot intent for.
     */
    private fun executeBotIntent(bot: MutableBot) {
        botIntentsMap[bot.id]?.apply {
            bot.speed = calcNewBotSpeed(bot.speed, targetSpeed ?: 0.0)
            bot.moveToNewPosition()

            updateBotTurnRatesAndDirections(bot, this)
            updateBotColors(bot, this)
            updateDebugGraphics(bot, this)
            processStdErrAndStdOut(bot, this)
            processTeamMessages(bot, this)
        }
    }

    /** Updates bullet positions */
    private fun updateBulletPositions() {
        bullets = bullets.map { it.copy(tick = it.tick + 1) }.toMutableSet()
    }

    /**
     * Checks and handles if the bots are inactive collectively.
     * That is when no bot have been hit by bullets for some time.
     */
    private fun checkAndHandleInactivity() {
        if (inactivityCounter++ > setup.maxInactivityTurns) {
            botsMap.values.forEach { it.addDamage(INACTIVITY_DAMAGE) }
        }
    }

    /** Check and handles if the bots have been disabled (when energy is zero or close to zero). */
    private fun checkForAndHandleDisabledBots() {
        botsMap.values.forEach { bot ->

            // If bot is disabled => Set then reset bot movement with the bot intent
            if (bot.isDisabled) {
                botIntentsMap[bot.id]?.disableMovement()
            }
        }
    }

    /** Checks and handles if any bots have been defeated. */
    private fun checkAndHandleDefeatedBots() {
        val deadBotIds =
            botsMap.values.filter { it.isDead }.map { bot -> participantIds.first { it.botId == bot.id } }.toSet()

        deadBotIds.forEach {
            val botDeathEvent = BotDeathEvent(turn.turnNumber, it.botId)
            turn.addPublicBotEvent(botDeathEvent)
            turn.addObserverEvent(botDeathEvent)
        }

        scoreTracker.registerDeaths(deadBotIds)
    }

    /** Checks the scan field for scanned bots. */
    private fun checkAndHandleScans() {
        val bots = botsMap.values.toList()
        for (i in bots.indices) {
            val scanningBot = bots[i]

            if (scanningBot.isDroid) continue // droids cannot use scanning

            val (startAngle, endAngle) = getScanAngles(scanningBot)

            for (j in bots.indices) {
                if (i != j) {
                    val botBeingScanned = bots[j]
                    if (isBotScanned(scanningBot, botBeingScanned, startAngle, endAngle)) {
                        handleScannedBot(scanningBot, botBeingScanned)
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
    private fun handleScannedBot(scanningBot: MutableBot, botBeingScanned: IBot) {
        createAndAddScannedBotEventToTurn(scanningBot.id, botBeingScanned)
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
        scanEndAngle: Double

    ) = isScanningOrMoving(scanningBot.id) &&
            isCircleIntersectingCircleSector(
                scannedBot.position, BOT_BOUNDING_CIRCLE_RADIUS,
                scanningBot.position, RADAR_RADIUS,
                scanStartAngle, scanEndAngle
            )

    /**
     * Checks if a bot is scanning, meaning that it must be either rescanning or moving.
     * @param botId is the id of the bot.
     * @return `true` if the bot is scanning; `false` otherwise.
     */
    private fun isScanningOrMoving(botId: BotId): Boolean {
        return isRescanning(botId) || isMoving(botId)
    }

    /**
     * Checks if a bot is rescanning.
     * @param botId is the id of the bot.
     * @return `true` if the bot is scanning; `false` otherwise.
     */
    private fun isRescanning(botId: BotId): Boolean {
        return botIntentsMap[botId]?.rescan ?: false
    }

    /**
     * Checks if a bot is moving, meaning that the x,y position or a direction has changed.
     * @param botId is the id of the bot.
     * @return `true` if the bot is moving; `false` otherwise.
     */
    private fun isMoving(botId: BotId): Boolean {
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
    private fun createAndAddScannedBotEventToTurn(scanningBotId: BotId, scannedBot: IBot) {
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

    /** Checks and handles if the round is ended or game is over. */
    private fun checkAndHandleRoundOrGameOver() {
        if (isRoundOver()) {
            round.apply {
                roundEnded = true
                if (roundNumber >= setup.numberOfRounds) {
                    gameState.isGameEnded = true // Game over
                }

                val scores = scoreCalculator.getScores()
                if (scores.isNotEmpty()) {
                    val winners = scores.filter { it.rank == 1 }
                    winners.forEach {
                        val botId = it.participantId.botId
                        turn.addPrivateBotEvent(botId, WonRoundEvent(turn.turnNumber))
                    }
                }
                accumulatedScoreCalculator.addScores(scores)
            }
        }
    }

    private fun isRoundOver() = run {
        // distinctBy(id) is necessary to take account for both bots and teams
        val aliveCount = getBotsOrTeams(MutableBot::isAlive).distinctBy { it.id }.count()
        if (aliveCount <= 1) {
            true
        } else {
            if (bullets.size > 0) {
                false
            } else {
                // When no bullets or functioning bots remain, the round ends immediately as a draw to speed things up
                val disabledCount = getBotsOrTeams(MutableBot::isDisabled).distinctBy { it.id }.count()
                disabledCount == aliveCount
            }
        }
    }

    private fun getBotsOrTeams(filter: (MutableBot) -> Boolean): Collection<ParticipantId> {
        val botIds = botsMap.values.filter { filter.invoke(it) }.map { it.id }

        return participantIds.filter { botIds.contains(it.botId) }.distinct()
    }

    private fun processTeamMessages(bot: MutableBot, intent: BotIntent) {
        intent.teamMessages?.let { teamMessages ->
            for (index in 0 until (teamMessages.size).coerceAtMost(MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN)) {
                teamMessages[index].let { teamMessage ->
                    teamMessage.apply {
                        if (message.length <= MAX_TEAM_MESSAGE_SIZE) { // ignore this and follower messages if one message is too big
                            if (receiverId != null) {
                                turn.addPrivateBotEvent(
                                    receiverId, TeamMessageEvent(turn.turnNumber, message, messageType, bot.id)
                                )
                            } else {
                                bot.teammateIds.forEach { teammateId ->
                                    turn.addPrivateBotEvent(
                                        teammateId, TeamMessageEvent(turn.turnNumber, message, messageType, bot.id)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        // clear team messages
        intent.teamMessages = null
    }


    /** for static methods */
    companion object {
        /**
         * Update bot turn rates and directions.
         * @param bot is the bot.
         * @param intent is the bot's intent.
         */
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

        /**
         * Updates the bot colors.
         * @param bot is the bot.
         * @param intent is the bot's intent.
         */
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

        private fun fromColor(color: String?) = color?.let { from(it) }

        /**
         * Updates last received data from standard output and standard error.
         * @param bot is the bot.
         * @param intent is the bot's intent.
         */
        private fun processStdErrAndStdOut(bot: MutableBot, intent: BotIntent) {
            // transfer from intent to state
            bot.apply {
                stdOut = intent.stdOut
                stdErr = intent.stdErr
            }
            // reset stdout and stderr
            intent.apply {
                stdOut = null
                stdErr = null
            }
        }
    }
}
