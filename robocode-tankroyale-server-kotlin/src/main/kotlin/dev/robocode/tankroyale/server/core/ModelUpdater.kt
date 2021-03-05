package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.server.event.*
import dev.robocode.tankroyale.server.math.*
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.rules.*
import dev.robocode.tankroyale.server.score.ScoreTracker
import java.util.*
import kotlin.math.abs
import kotlin.math.atan2


/** Maximum bounding circle diameter of a bullet moving with max speed */
private val BULLET_MAX_BOUNDING_CIRCLE_DIAMETER: Double = 2 * MAX_BULLET_SPEED

/** Square of maximum bounding circle diameter of a bullet moving with max speed */
private val BULLET_MAX_BOUNDING_CIRCLE_DIAMETER_SQUARED: Double =
    BULLET_MAX_BOUNDING_CIRCLE_DIAMETER * BULLET_MAX_BOUNDING_CIRCLE_DIAMETER

/** Square of the bounding circle diameter of a bot */
private const val BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED: Double =
    BOT_BOUNDING_CIRCLE_DIAMETER.toDouble() * BOT_BOUNDING_CIRCLE_DIAMETER


/** Model updater, which is used for keeping track of the model state for each turn and round of a game. */
class ModelUpdater(
    /** Game setup */
    private val setup: GameSetup,
    /** Participant ids */
    private val participantIds: Set<BotId>
) {
    /** Score keeper */
    private val scoreTracker: ScoreTracker = ScoreTracker(participantIds)

    /** Map over all bots */
    private val botsMap = mutableMapOf<BotId, MutableBot>()

    /** Map over all bot intents */
    private val botIntentsMap = mutableMapOf<BotId, BotIntent>()

    /** Bullets */
    private val bullets = mutableSetOf<MutableBullet>()

    /** Game state */
    private var gameState = GameState(Arena(setup.arenaWidth, setup.arenaHeight))

    /** Round record */
    private var round = MutableRound(0)

    /** Turn record */
    val turn = MutableTurn(0)

    /** Id for the next bullet that comes into existence */
    private var nextBulletId = 0

    /** Inactivity counter */
    private var inactivityCounter = 0

    /** The current results ordered with highest total scores first */
    val results: List<Score> get() = scoreTracker.results

    /** The number of rounds played so far */
    val numberOfRounds: Int get() = gameState.rounds.size

    /**
     * Updates game state.
     * @param botIntents is the bot intents, which gives instructions to the game from the individual bots.
     * @return new game state when the game state has been updated.
     */
    fun update(botIntents: Map<BotId, IBotIntent>): GameState {
        updateBotIntents(botIntents)
        if (round.roundEnded) {
            calculatePlacements()
            nextRound()
        } else if (round.roundNumber == 0 && turn.turnNumber == 0) {
            nextRound()
        }
        nextTurn()
        return updateGameState()
    }

    /** Calculates and sets placements for all bots, i.e. 1st, 2nd, and 3rd places. */
    fun calculatePlacements() {
        scoreTracker.calculatePlacements()
    }

    /**
     * Updates the current bot intents with the new bot intents.
     * @param botIntents is a map of new bot intents.
     */
    private fun updateBotIntents(botIntents: Map<BotId, IBotIntent>) {
        for ((botId, updateIntent) in botIntents.entries) {
            val botIntent = botIntentsMap[botId] ?: BotIntent()
            botIntent.update(updateIntent)
            botIntentsMap[botId] = botIntent
        }
    }

    /** Proceed with the next round. */
    private fun nextRound() {
        round = round.copy(roundNumber = round.roundNumber)
        round.roundEnded = false
        round.roundNumber++

        turn.turnNumber = 0

        nextBulletId = 0
        botIntentsMap.clear()
        bullets.clear()
        botsMap.clear()
        scoreTracker.prepareRound()
        inactivityCounter = 0

        initializeBotStates()
    }

    /** Proceed with the next turn. */
    private fun nextTurn() {
        // Reset events
        turn.turnNumber++
        turn.resetEvents()

        // Remove dead bots (cannot participate in new round)
        botsMap.values.removeIf(IBot::isDead)

        // Note: Called here before updating headings as we need to sync firing the gun with the gun's direction.
        // That is if the gun was set to fire with the last turn, then it will fire in the correct gun heading now.
        coolDownAndFireGuns()

        executeBotIntents()
        checkAndHandleScans()
        checkAndHandleBotWallCollisions()
        checkAndHandleBotCollisions()
        constrainBotPositions()
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
    }

    /**
     * Updates the game state.
     * @return new game state.
     */
    private fun updateGameState(): GameState {
        round.turns += turn.toTurn()
        gameState.rounds += round.toRound()
        return gameState
    }

    /** Initializes bot states. */
    private fun initializeBotStates() {
        val occupiedCells = mutableSetOf<Int>()
        for (id in participantIds) {
            val position = randomBotPosition(occupiedCells)
            val direction = randomDirection() // body, gun, and radar starts in the same direction
            botsMap[id] = MutableBot(
                id = id,
                position = position.toMutablePoint(),
                direction = direction,
                gunDirection = direction,
                radarDirection = direction,
            )
        }
        // Store bot snapshots into the turn
        turn.copyBots(botsMap.values)
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
        if (cellCount < numBots) {
            throw IllegalArgumentException(
                "Area size (${setup.arenaWidth},${setup.arenaHeight}) is too small to contain $numBots bots"
            )
        }
        val cellWidth = setup.arenaWidth / gridWidth
        val cellHeight = setup.arenaHeight / gridHeight

        return randomBotPoint(occupiedCells, cellCount, gridWidth, cellWidth, cellHeight)
    }

    /** Execute bot intents for all bots that are not disabled */
    private fun executeBotIntents() {
        for (bot in botsMap.values) {
            if (bot.isEnabled) updateBotStates(bot)
        }
    }

    /**
     * Updates the bot states (position, speed, turn rates, angles, colors etc.)
     * @param bot it the bot top execute the bot intent for.
     */
    private fun updateBotStates(bot: MutableBot) {
        val intent = botIntentsMap[bot.id]
        intent?.apply {
            bot.speed = calcNewBotSpeed(bot.speed, intent.targetSpeed ?: 0.0)

            updateBotTurnRatesAndDirections(bot, intent)
            updateBotColors(bot, intent)

            bot.moveToNewPosition()
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
                continue // A bot cannot shot itself
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
        val victimId = bot.id

        inactivityCounter = 0 // reset collective inactivity counter due to bot taking bullet damage

        val damage = calcBulletDamage(bullet.power)
        val isKilled = bot.addDamage(damage)

        val energyBonus = BULLET_HIT_ENERGY_GAIN_FACTOR * bullet.power
        botsMap[botId]?.changeEnergy(energyBonus)

        scoreTracker.registerBulletHit(botId, victimId, damage, isKilled)

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

    /** Constrain all bot positions so they are kept inside the battle arena. */
    private fun constrainBotPositions() {
        botsMap.values.forEach { bot -> run {
            val (x, y) = constrainBotPosition(bot.x, bot.y)
            bot.x = x
            bot.y = y
        }}
    }

    /**
     * Constrain the bot position so it is kept inside the battle arena.
     *
     * @param x is the current x coordinate of the bot position.
     * @param y is the current y coordinate of the bot position.
     * return new (x, y) coordinates that has been constrained.
     */
    private fun constrainBotPosition(x: Double, y: Double): Pair<Double, Double> {
        var newX = x
        var newY = y
        when {
            x - BOT_BOUNDING_CIRCLE_RADIUS < 0 -> {
                newX = BOT_BOUNDING_CIRCLE_RADIUS
            }
            x + BOT_BOUNDING_CIRCLE_RADIUS > setup.arenaWidth -> {
                newX = setup.arenaWidth - BOT_BOUNDING_CIRCLE_RADIUS
            }
            y - BOT_BOUNDING_CIRCLE_RADIUS < 0 -> {
                newY = BOT_BOUNDING_CIRCLE_RADIUS
            }
            y + BOT_BOUNDING_CIRCLE_RADIUS > setup.arenaHeight -> {
                newY = setup.arenaHeight - BOT_BOUNDING_CIRCLE_RADIUS
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

        // Both bots takes damage when hitting each other
        registerRamHit(bot1, bot2, isBot1RammingBot2, isBot2RammingBot1)

        // Restore both bot's old position
        val oldPos1 = round.lastTurn?.getBot(bot1.id)!!.position
        val oldPos2 = round.lastTurn?.getBot(bot2.id)!!.position
        bot1.position = MutablePoint(oldPos1.x, oldPos1.y)
        bot2.position = MutablePoint(oldPos2.x, oldPos2.y)

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
        // Both bots takes damage when hitting each other
        val bot1Killed = bot1.addDamage(RAM_DAMAGE)
        val bot2Killed = bot2.addDamage(RAM_DAMAGE)
        if (isBot1RammingBot2) {
            scoreTracker.registerRamHit(bot1.id, bot2.id, bot2Killed)
        }
        if (isBot2RammingBot1) {
            scoreTracker.registerRamHit(bot2.id, bot1.id, bot1Killed)
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
            val (x, y) = constrainBotPosition(bot.x, bot.y)
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
        for (bot in botsMap.values) {
            if (bot.energy < 0.01 && bot.energy > 0.0) {
                bot.energy = 0.0
            }
            // If bot is disabled => Set then reset bot movement with the bot intent
            if (bot.energy == 0.0) {
                botIntentsMap[bot.id]?.disableMovement()
            }
        }
    }

    /** Checks and handles if any bots have been defeated. */
    private fun checkAndHandleDefeatedBots() {
        for (bot in botsMap.values) {
            if (bot.isDead) {
                val botDeathEvent = BotDeathEvent(turn.turnNumber, bot.id)
                turn.addPublicBotEvent(botDeathEvent)
                turn.addObserverEvent(botDeathEvent)
                scoreTracker.registerBotDeath(bot.id)
            }
        }
    }

    /** Cool down and fire guns. */
    private fun coolDownAndFireGuns() {
        for (bot in botsMap.values) {
            // If gun heat is zero and the bot is enabled, it is able to fire
            if (bot.gunHeat == 0.0 && bot.isEnabled) { // Gun can fire
                checkWhetherToFireGun(bot)
            } else {// Gun is too hot => Cool down gun
                coolDownGun(bot)
            }
        }
    }

    /**
     * Checks and determines if the gun for a bot must be fired.
     * @param bot is the bot.
     */
    private fun checkWhetherToFireGun(bot: MutableBot) {
        val botIntent = botIntentsMap[bot.id]
        if (botIntent != null) {
            val firepower = botIntent.bulletPower ?: 0.0
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
        firepower.coerceAtMost(MAX_FIREPOWER)

        bot.gunHeat = calcGunHeat(firepower)
        val bullet = MutableBullet(
            botId = bot.id,
            id = BulletId(++nextBulletId),
            startPosition = bot.position.toPoint(),
            direction = bot.gunDirection,
            power = firepower,
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
            val (startAngle, endAngle) = getScanAngles(scanningBot)

            for (j in bots.indices) {
                if (i != j) {
                    val botBeingScanned = bots[j]
                    if (isBotScanned(scanningBot, botBeingScanned, startAngle, endAngle)) {
                        createAndAddScannedBotEventToTurn(scanningBot, botBeingScanned)
                    }
                }
            }
        }
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
    ): Boolean =
        isCircleIntersectingCircleSector(
            scannedBot.position, BOT_BOUNDING_CIRCLE_RADIUS,
            scanningBot.position, RADAR_RADIUS,
            scanStartAngle, scanEndAngle
        )

    /**
     * Creates and adds scanned-bot-events to the turn.
     * @param scanningBot is the bot performing the scanning.
     * @param scannedBot is the bot exposed for scanning.
     */
    private fun createAndAddScannedBotEventToTurn(scanningBot: IBot, scannedBot: IBot) {
        val scannedBotEvent = ScannedBotEvent(
            turn.turnNumber,
            scanningBot.id,
            scannedBot.id,
            scannedBot.energy,
            scannedBot.x,
            scannedBot.y,
            scannedBot.direction,
            scannedBot.speed
        )
        turn.addPrivateBotEvent(scanningBot.id, scannedBotEvent)
        turn.addObserverEvent(scannedBotEvent)
    }

    /**
     * Returns the scan angles for a bot.
     * @param bot is the bot.
     * @return a pair of doubles, where the first double is the start angle, and the second double is the end angle.
     */
    private fun getScanAngles(bot: IBot): Pair<Double, Double> {
        val spreadAngle = bot.scanSpreadAngle
        val startAngle: Double
        val endAngle: Double
        if (spreadAngle > 0) {
            endAngle = bot.scanDirection
            startAngle = normalAbsoluteDegrees(endAngle - spreadAngle)
        } else {
            startAngle = bot.scanDirection
            endAngle = normalAbsoluteDegrees(startAngle - spreadAngle)
        }
        return Pair(startAngle, endAngle)
    }

    /** Checks and handles if the round is ended or game is over. */
    private fun checkAndHandleRoundOrGameOver() {
        if (botsMap.size <= 1) {
            round.roundEnded = true // Round ended
            if (round.roundNumber >= setup.numberOfRounds) {
                gameState.isGameEnded = true // Game over
            }
        }
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
            if (abs(dx) > BULLET_MAX_BOUNDING_CIRCLE_DIAMETER) {
                return false
            }
            val dy = pos2.y - pos1.y
            return abs(dy) <= BULLET_MAX_BOUNDING_CIRCLE_DIAMETER &&
                    ((dx * dx) + (dy * dy) <= BULLET_MAX_BOUNDING_CIRCLE_DIAMETER_SQUARED)
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
            val bearing = normalRelativeDegrees(Math.toDegrees(angle) - bot.direction)
            return (((bot.speed > 0 && (bearing > -90 && bearing < 90))
                    || (bot.speed < 0 && (bearing < -90 || bearing > 90))))
        }

        /**
         * Returns a random point for a arena of split into x * y virtual and big square cells larger than the bot size.
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
         * Updates scan direction and scan spread for a bot.
         * @param bot is the bot.
         * @param intent is the bot´s intent.
         * @param newRadarDirection is the new radar direction for the bot.
         */
        private fun updateScanDirectionAndSpread(bot: MutableBot, intent: BotIntent, newRadarDirection: Double) {
            // The radar sweep is the difference between the new and old radar direction
            val newSpreadAngle = normalRelativeDegrees(newRadarDirection - bot.radarDirection)
            val scan = intent.scan ?: false
            bot.scanDirection = if (scan) bot.radarDirection else newRadarDirection
            bot.scanSpreadAngle = if (scan) bot.radarSpreadAngle else newSpreadAngle
            bot.radarDirection = newRadarDirection
            bot.radarSpreadAngle = newSpreadAngle
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

            var adjustmentRate = turnRate
            bot.direction = normalAbsoluteDegrees(bot.direction + adjustmentRate)

            // Gun direction depends on the turn rate of both the body and the gun
            adjustmentRate += gunTurnRate
            if (intent.adjustGunForBodyTurn == true) {
                adjustmentRate -= turnRate
            }
            adjustmentRate = limitGunTurnRate(adjustmentRate)
            bot.gunDirection = normalAbsoluteDegrees(bot.gunDirection + adjustmentRate)

            // Radar direction depends on the turn rate of the body, the gun, and the radar
            adjustmentRate += radarTurnRate
            if (intent.adjustRadarForGunTurn == true) {
                adjustmentRate -= gunTurnRate
            }
            adjustmentRate = limitRadarTurnRate(adjustmentRate)
            val radarDirection = normalAbsoluteDegrees(bot.radarDirection + adjustmentRate)

            updateScanDirectionAndSpread(bot, intent, radarDirection)

            bot.turnRate = turnRate
            bot.gunTurnRate = gunTurnRate
            bot.radarTurnRate = radarTurnRate
        }

        /**
         * Updates the bot colors.
         * @param bot is the bot.
         * @param intent is the bot´s intent.
         */
        private fun updateBotColors(bot: MutableBot, intent: BotIntent) {
            bot.bodyColor = colorStringToRGB(intent.bodyColor)
            bot.turretColor = colorStringToRGB(intent.turretColor)
            bot.radarColor = colorStringToRGB(intent.radarColor)
            bot.bulletColor = colorStringToRGB(intent.bulletColor)
            bot.scanColor = colorStringToRGB(intent.scanColor)
            bot.tracksColor = colorStringToRGB(intent.tracksColor)
            bot.gunColor = colorStringToRGB(intent.gunColor)
        }
    }
}