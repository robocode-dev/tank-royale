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
import dev.robocode.tankroyale.server.Server
import java.lang.Math.toDegrees
import java.util.*
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
    internal val botsMap = mutableMapOf<BotId, MutableBot>()

    /** Map over copied bots from previous turn */
    private val botsCopies = mutableMapOf<BotId, MutableBot>()

    /** Map over all bot intents */
    private val botIntentsMap = mutableMapOf<BotId, BotIntent>()

    /** Bullets */
    private val bullets = mutableSetOf<MutableBullet>()

    /** Game state */
    private var gameState = GameState(Arena(setup.arenaWidth, setup.arenaHeight))

    /** Round record */
    private var round = MutableRound(0)

    /** Turn record */
    internal val turn = MutableTurn(0)

    /** The id for the next bullet that comes into existence */
    private var nextBulletId = 0

    /** Inactivity counter */
    private var inactivityCounter = 0

    /** The accumulated results ordered with higher total scores first */
    internal fun getResults() = accumulatedScoreCalculator.getScores()

    /** The number of rounds played so far */
    internal val numberOfRounds: Int get() = gameState.rounds.size

    internal fun isAlive(botId: BotId) = botsMap[botId]?.isAlive ?: false

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
        }
        turn.turnNumber = 0

        nextBulletId = 0
        botIntentsMap.clear()
        bullets.clear()
        botsMap.clear()
        scoreTracker.clear()
        inactivityCounter = 0

        initializeBotStates()
    }

    /** Proceed with the next turn. */
    private fun nextTurn() {
        // Reset events
        turn.turnNumber++
        turn.resetEvents()

        deepCopyBots()

        coolDownAndFireGuns()

        executeBotIntents()

        checkAndHandleBotWallCollisions()
        checkAndHandleBotCollisions()
        constrainBotPositions()

        checkAndHandleScans()

        updateBulletPositions()
        checkAndHandleBulletWallCollisions()
        checkAndHandleBulletHits()

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
        position = MutablePoint(bot.x, bot.y),
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
        if (gameState.rounds.size == 0 || gameState.rounds.last().roundNumber != round.roundNumber) {
            gameState.rounds += round
        }
        return gameState
    }

    /** Initializes bot states. */
    private fun initializeBotStates() {
        val occupiedCells = mutableSetOf<Int>()
        for (teamOrBotId in participantIds) {
            val botId = teamOrBotId.botId

            val isDroid = droidFlags[botId] ?: false
            val energy = if (isDroid) INITIAL_DROID_ENERGY else INITIAL_BOT_ENERGY

            val randomPosition = randomBotPosition(occupiedCells)
            val position = adjustForInitialPosition(botId, randomPosition)
            // note: body, gun, and radar starts in the same direction
            val randomDirection = randomDirection()
            val direction = adjustForInitialAngle(botId, randomDirection)

            val teammateIds: Set<BotId> =
                teamOrBotId.teamId?.let {
                    participantIds.filter { it.teamId == teamOrBotId.teamId }.map { it.botId }.toSet()
                        .minus(botId)
                } ?: emptySet()

            botsMap[botId] = MutableBot(
                id = botId,
                isDroid = isDroid,
                energy = energy,
                teammateIds = teammateIds,
                position = position.toMutablePoint(),
                direction = direction,
                gunDirection = direction,
                radarDirection = direction,
            )
        }
        // Store bot snapshots into the turn
        turn.copyBots(botsMap.values)
    }

    private fun adjustForInitialPosition(botId: BotId, point: Point): Point {
        if (!Server.initialPositionEnabled) return point
        val initialPosition = initialPositions[botId]
        return if (initialPosition == null) {
            point
        } else {
            val x = clamp(
                initialPosition.x ?: point.x,
                BOT_BOUNDING_CIRCLE_RADIUS,
                setup.arenaWidth - BOT_BOUNDING_CIRCLE_RADIUS
            )
            val y = clamp(
                initialPosition.y ?: point.y,
                BOT_BOUNDING_CIRCLE_RADIUS,
                setup.arenaHeight - BOT_BOUNDING_CIRCLE_RADIUS
            )
            Point(x, y)
        }
    }

    private fun adjustForInitialAngle(botId: BotId, direction: Double): Double {
        if (!Server.initialPositionEnabled) return direction
        val initialPosition = initialPositions[botId]
        return if (initialPosition == null) {
            direction
        } else {
            initialPosition.direction ?: direction
        }
    }

    /**
     * Calculates a random bot position.
     * @param occupiedCells is the occupied cells, where other bots are already positioned.
     * @return a random bot position
     */
    private fun randomBotPosition(occupiedCells: MutableSet<Int>): Point {
        val gridWidth = setup.arenaWidth / 50
        val gridHeight = setup.arenaHeight / 50
        val cellCount = gridWidth * gridHeight
        val numBots = participantIds.size
        require(cellCount >= numBots) {
            "Area size (${setup.arenaWidth},${setup.arenaHeight}) is too small to contain $numBots bots"
        }
        val cellWidth = setup.arenaWidth / gridWidth
        val cellHeight = setup.arenaHeight / gridHeight

        return randomBotPoint(occupiedCells, cellCount, gridWidth, cellWidth, cellHeight)
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

    /** Checks and handles bullet hits. */
    private fun checkAndHandleBulletHits() {
        val bulletCount = bullets.size
        if (bulletCount > 0) {
            // Create list of bullet line segments used for checking for bullet hits
            val bulletLines = mutableListOf<BulletLine>()
            bullets.forEach { bulletLines += BulletLine(it.toBullet()) }

            // Check for bullet hits
            for (i in 0 until bulletCount) {
                for (j in i + 1 until bulletCount) {
                    if (isColliding(bulletLines[i], bulletLines[j])) {
                        handleBulletHitBullet(bulletLines[i].bullet, bulletLines[j].bullet)
                    }
                }
                checkAndHandleBulletHitBot(bulletLines[i])
            }
        }
    }

    /**
     * Handles when a bullet has hit another bullet.
     * @param bullet1 is the first bullet.
     * @param bullet2 is the second bullet.
     */
    private fun handleBulletHitBullet(bullet1: IBullet, bullet2: IBullet) {
        val event1 = BulletHitBulletEvent(turn.turnNumber, bullet1, bullet2)
        val event2 = BulletHitBulletEvent(turn.turnNumber, bullet2, bullet1)

        turn.apply {
            addPrivateBotEvent(bullet1.botId, event1)
            addPrivateBotEvent(bullet2.botId, event2)
            // Observers only need a single event
            addObserverEvent(event1)
        }
        // Remove bullets from the arena
        bullets -= (bullet1 as Bullet).toMutableBullet()
        bullets -= (bullet2 as Bullet).toMutableBullet()
    }

    /**
     * Checks if two bullet bullets are colliding, i.e. if their two line segments are intersecting.
     * @return `true` if the two bullet line segments are intersection; `false` otherwise.
     */
    private fun isColliding(bulletLine1: BulletLine, bulletLine2: BulletLine): Boolean =
        // Check if the bullets bounding circles intersects (is fast) before
        isBulletsMaxBoundingCirclesColliding(bulletLine1.end, bulletLine2.end) &&
                // checking if the bullets bounding lines intersect (is slower)
                isLineIntersectingLine(bulletLine1.line, bulletLine2.line)

    /**
     * Checks and handles if a bullet hits a bot.
     * @param bulletLine is the bullet line of the bullet.
     */
    private fun checkAndHandleBulletHitBot(bulletLine: BulletLine) {
        // Check bullet-hit-bot collision (hit)
        for (bot in botsMap.values) {
            if (bulletLine.bullet.botId == bot.id) {
                continue // A bot cannot shoot itself
            }
            if (isBulletHittingBot(bulletLine, bot)) {
                handleBulletHittingBot(bulletLine.bullet, bot)

                // Remove bullet from the arena
                bullets.removeIf { bullet -> bullet.id == bulletLine.bullet.id }
            }
        }
    }

    /**
     * Checks if a bullet is hitting a bot.
     * @param bulletLine is the line of the bullet.
     * @param bot is the bot that might be hit.
     * @return `true` if the bot has been hit; `false` otherwise.
     */
    private fun isBulletHittingBot(bulletLine: BulletLine, bot: IBot): Boolean =
        isLineIntersectingCircle(bulletLine.line, bot.position, BOT_BOUNDING_CIRCLE_RADIUS)

    /**
     * Handles when a bullet has hit a bot.
     * @param bullet is the bullet that has hit.
     * @param bot is the bot that have been hit.
     */
    private fun handleBulletHittingBot(bullet: IBullet, bot: MutableBot) {
        val botId = bullet.botId
        val teamOrBotId = participantIds.first { it.botId == botId }
        val victimId = bot.id
        val victimTeamOrBotId = participantIds.first { it.botId == victimId }

        inactivityCounter = 0 // reset collective inactivity counter due to bot taking bullet damage

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
            addPrivateBotEvent(bulletHitBotEvent.bullet.botId, bulletHitBotEvent) // Bot itself gets event
            addPrivateBotEvent(bulletHitBotEvent.victimId, bulletHitBotEvent) // Victim bot gets event too
            addObserverEvent(bulletHitBotEvent)
        }
    }

    /** Check collisions between bots */
    private fun checkAndHandleBotCollisions() {
        val bots = botsMap.values.toList()
        for (i in bots.indices) {
            for (j in i + 1 until bots.size) {
                if (isBotsBoundingCirclesColliding(bots[i], bots[j])) {
                    handleBotHitBot(bots[i], bots[j])
                }
            }
        }
    }

    /** Constrain all bot positions, so they are kept inside the battle arena. */
    private fun constrainBotPositions() {
        botsMap.values.forEach { bot ->
            val (previousX, previousY) = botsCopies[bot.id]!!.position
            val (x, y) = constrainBotPosition(previousX, previousY, bot.x, bot.y)
            bot.x = x
            bot.y = y
        }
    }

    /**
     * Constrain the bot position, so it is kept inside the battle arena.
     *
     * @param x is the current x coordinate of the bot position.
     * @param y is the current y coordinate of the bot position.
     * @return new (x, y) coordinates that has been constrained.
     */
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

    /**
     * Handles when a bot and hit another bot.
     * @param bot1 is the first bot.
     * @param bot2 is the second bot.
     */
    private fun handleBotHitBot(bot1: MutableBot, bot2: MutableBot) {
        val isBot1RammingBot2 = isRamming(bot1, bot2)
        val isBot2RammingBot1 = isRamming(bot2, bot1)

        // Both bots take damage when hitting each other
        registerRamHit(bot1, bot2, isBot1RammingBot2, isBot2RammingBot1)

        // Restore both bot´s old position
        val lastTurn = round.lastTurn
        if (turn.turnNumber == 1 || lastTurn == null) {
            // Same position on first turn? => Move the second bot to a random position
            val x = BOT_BOUNDING_CIRCLE_RADIUS + Math.random() * (setup.arenaWidth - BOT_BOUNDING_CIRCLE_DIAMETER)
            val y = BOT_BOUNDING_CIRCLE_RADIUS + Math.random() * (setup.arenaHeight - BOT_BOUNDING_CIRCLE_DIAMETER)
            bot2.position = MutablePoint(x, y)
        } else {
            val oldPos1 = lastTurn.getBot(bot1.id)!!.position
            val oldPos2 = lastTurn.getBot(bot2.id)!!.position
            bot1.position = MutablePoint(oldPos1.x, oldPos1.y)
            bot2.position = MutablePoint(oldPos2.x, oldPos2.y)
        }

        // Stop bots by setting speed to 0
        if (isBot1RammingBot2) bot1.speed = 0.0
        if (isBot2RammingBot1) bot2.speed = 0.0

        // Create bot-hit-bot events
        val event1 = BotHitBotEvent(turn.turnNumber, bot1.id, bot2.id, bot2.energy, bot2.x, bot2.y, isBot1RammingBot2)
        val event2 = BotHitBotEvent(turn.turnNumber, bot2.id, bot1.id, bot1.energy, bot1.x, bot1.y, isBot2RammingBot1)
        turn.apply {
            addPrivateBotEvent(bot1.id, event1)
            addPrivateBotEvent(bot2.id, event2)
            addObserverEvent(event1)
            addObserverEvent(event2)
        }
    }

    /**
     * Registers a ram hit.
     * @param bot1 is the first bot.
     * @param bot2 is the second bot.
     * @param isBot1RammingBot2 is `true` if `bot1` has rammed `bot2`; `false` otherwise.
     * @param isBot2RammingBot1 is `true` if `bot2` has rammed `bot1`; `false` otherwise.
     */
    private fun registerRamHit(
        bot1: MutableBot,
        bot2: MutableBot,
        isBot1RammingBot2: Boolean,
        isBot2RammingBot1: Boolean
    ) {
        // Both bots take damage when hitting each other
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

    /** Updates bullet positions */
    private fun updateBulletPositions() {
        bullets.forEach { bullet -> bullet.incrementTick() }
    }

    /** Checks collisions between bots and the walls. */
    private fun checkAndHandleBotWallCollisions() {
        for (bot in botsMap.values) {
            val hitWall = adjustBotCoordinatesIfHitWall(bot)
            if (hitWall) {
                // Omit sending hit-wall-event if the bot hit the wall in the previous turn
                if (round.lastTurn!!.getEvents(bot.id).none { event -> event is BotHitWallEvent }) {

                    val botHitWallEvent = BotHitWallEvent(turn.turnNumber, bot.id)
                    turn.addPrivateBotEvent(bot.id, botHitWallEvent)
                    turn.addObserverEvent(botHitWallEvent)

                    bot.addDamage(calcWallDamage(bot.speed))
                }
                // Bot is stopped to zero speed regardless of its previous direction
                bot.speed = 0.0
            }
        }
    }

    /**
     * Adjust the coordinates of the bot, if it has hit the wall.
     * If the (x,y) coordinate is adjusted, the direction of the bot is used for calculating the new (x,y).
     */
    private fun adjustBotCoordinatesIfHitWall(bot: MutableBot): Boolean {
        var hitWall = false
        if (round.lastTurn != null) {
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

    /** Checks collisions between the bullets and the walls. */
    private fun checkAndHandleBulletWallCollisions() {
        val iterator = bullets.iterator() // due to removal
        while (iterator.hasNext()) {
            val bullet = iterator.next()
            if (isPointOutsideArena(bullet.position())) {
                // remove bullet from arena
                iterator.remove()

                val bulletHitWallEvent = BulletHitWallEvent(turn.turnNumber, bullet.copy())
                turn.addPrivateBotEvent(bullet.botId, bulletHitWallEvent)
                turn.addObserverEvent(bulletHitWallEvent)
            }
        }
    }

    /**
     * Checks if a point is outside the arena.
     * @param point is the point
     * @return `true` if the point is outside the arena; `false` otherwise.
     */
    private fun isPointOutsideArena(point: IPoint): Boolean {
        return point.x <= 0 ||
                point.y <= 0 ||
                point.x >= setup.arenaWidth ||
                point.y >= setup.arenaHeight
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

    /** Cool down and fire guns. */
    private fun coolDownAndFireGuns() {
        botsMap.values.forEach { bot ->
            // If gun heat is zero and the bot is enabled, it is able to fire
            if (bot.gunHeat == 0.0 && bot.isEnabled) { // Gun can fire
                checkIfGunMustFire(bot)
            } else { // Gun is too hot => Cool down gun
                coolDownGun(bot)
            }
        }
    }

    /**
     * Checks and determines if the gun for a bot must be fired.
     * @param bot is the bot.
     */
    private fun checkIfGunMustFire(bot: MutableBot) {
        botIntentsMap[bot.id]?.let {
            val firepower = it.firepower ?: 0.0
            if (firepower >= MIN_FIREPOWER && bot.energy > firepower) {
                fireBullet(bot, firepower)
            }
        }
    }

    /**
     * Cools down gun for a bot.
     * @param bot is the bot.
     */
    private fun coolDownGun(bot: MutableBot) {
        bot.gunHeat = (bot.gunHeat - setup.gunCoolingRate).coerceAtLeast(0.0)
    }

    /**
     * Fires a bullet for a bot.
     * @param bot is the bot.
     * @param firepower is the amount of firepower.
     */
    private fun fireBullet(bot: MutableBot, firepower: Double) {
        val power = firepower.coerceAtMost(MAX_FIREPOWER)

        val previousBotState = botsCopies[bot.id]!!
        var fireDirection = bot.gunDirection

        // fire assistance (fireAssist = true, bot is scanning other bot, and gun and radar angle must be the same
        if (botIntentsMap[bot.id]?.fireAssist == true &&
            bot.gunDirection == bot.radarDirection &&
            previousBotState.gunDirection == previousBotState.radarDirection
        ) {
            round.lastTurn?.let { previousTurn ->
                previousTurn.botEvents[bot.id]?.find { it is ScannedBotEvent }?.let {
                    val scan = (it as ScannedBotEvent)
                    fireDirection = angle(bot.x, bot.y, scan.x, scan.y) // fire assisted angle
                }
            }
        }

        bot.gunHeat = calcGunHeat(power)

        val bullet = MutableBullet(
            id = BulletId(++nextBulletId),
            botId = bot.id,
            startPosition = bot.position.toPoint(),
            direction = fireDirection,
            power = power,
            color = bot.bulletColor,
        )
        bullets += bullet

        val bulletFiredEvent = BulletFiredEvent(turn.turnNumber, bullet.copy())
        turn.addPrivateBotEvent(bot.id, bulletFiredEvent)
        turn.addObserverEvent(bulletFiredEvent)

        // Firing a bullet cost energy
        bot.changeEnergy(-firepower)
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
         * Checks if the maximum bounding circles of two bullets are colliding.
         * This is a pre-check if two bullets might be colliding.
         * @param pos1 is the position of the 1st bullet.
         * @param pos2 is the position of the 2nd bullet.
         * @return `true` if the bounding circles are colliding; `false` otherwise.
         */
        private fun isBulletsMaxBoundingCirclesColliding(pos1: IPoint, pos2: IPoint): Boolean {
            val dx = pos2.x - pos1.x
            if (abs(dx) > bulletMaxBoundingCircleDiameter) {
                return false
            }
            val dy = pos2.y - pos1.y
            return abs(dy) <= bulletMaxBoundingCircleDiameter &&
                    ((dx * dx) + (dy * dy) <= bulletMaxBoundingCircleDiameterSquared)
        }

        /**
         * Checks if the bounding circles of two bots are colliding.
         * @param bot1 is the first bot.
         * @param bot2 is the second bot.
         * @return `true` if the bounding circles are colliding; `false` otherwise.
         */
        private fun isBotsBoundingCirclesColliding(bot1: IBot, bot2: IBot): Boolean {
            val dx = bot2.x - bot1.x
            if (abs(dx) > BOT_BOUNDING_CIRCLE_DIAMETER) { // 2 x radius
                return false
            }
            val dy = bot2.y - bot1.y
            // 2 x radius
            return abs(dy) <= BOT_BOUNDING_CIRCLE_DIAMETER &&
                    ((dx * dx) + (dy * dy) <= BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED)
        }

        /**
         * Checks if a bot is ramming another bot.
         * @param bot is the potential ramming bot.
         * @param victim is the potential bot being victim of ramming.
         * @return `true` if the bot is ramming the victim bot; `false` otherwise.
         */
        private fun isRamming(bot: IBot, victim: IBot): Boolean {
            val dx = victim.x - bot.x
            val dy = victim.y - bot.y
            val angle = atan2(dy, dx)
            val bearing = normalizeRelativeDegrees(toDegrees(angle) - bot.direction)
            return (((bot.speed > 0 && (bearing > -90 && bearing < 90))
                    || (bot.speed < 0 && (bearing < -90 || bearing > 90))))
        }

        /**
         * Returns a random point for an arena of split into x * y virtual and big square cells larger than the bot size.
         * The idea is that only one bot can occupy a specific cell. So the number of cells limits how many bots that
         * can be placed on the arena. Hence, the lesser the sizes of the cells are, the more bots can be placed on the
         * arena.
         * @param occupiedCells is a set of cell occupied indices. So if the arena is split into e.g. 3 x 2 cells,
         * the total number of cells will be 6, and the indices 1, 2, 3 will be the indices of the cells for the first
         * row, and the indices 4, 5, 6 will be the indices of the cells for the second row.
         * @param cellCount is the total number of cells.
         * @param gridWidth is the number cells per row.
         * @param cellWidth is the width of each cell measured in virtual pixels.
         * @param cellHeight is the height of each cell measured in virtual pixels.
         * @return a random point on the arena in an unoccupied cell.
         */
        private fun randomBotPoint(
            occupiedCells: MutableSet<Int>,
            cellCount: Int,
            gridWidth: Int,
            cellWidth: Int,
            cellHeight: Int
        ): Point {
            while (true) {
                val cell = Random().nextInt(cellCount)
                if (!occupiedCells.contains(cell)) {
                    occupiedCells += cell
                    var y = (cell / gridWidth).toDouble()
                    var x = cell - y * gridWidth
                    x *= cellWidth.toDouble()
                    y *= cellHeight.toDouble()
                    x += BOT_BOUNDING_CIRCLE_RADIUS + Math.random() * (cellWidth - BOT_BOUNDING_CIRCLE_DIAMETER)
                    y += BOT_BOUNDING_CIRCLE_RADIUS + Math.random() * (cellHeight - BOT_BOUNDING_CIRCLE_DIAMETER)
                    return Point(x, y)
                }
            }
        }

        /**
         * Update bot turn rates and directions.
         * @param bot is the bot.
         * @param intent is the bot´s intent.
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
         * @param intent is the bot´s intent.
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
         * @param intent is the bot´s intent.
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