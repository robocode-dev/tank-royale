package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.server.event.*
import dev.robocode.tankroyale.server.math.*
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.rules.*
import dev.robocode.tankroyale.server.score.ScoreTracker
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
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


/** Model updater, which keep track of the model state for each turn of a game. */
class ModelUpdater(
    /** Game setup */
    private val setup: GameSetup,
    /** Participant ids */
    private val participantIds: Set<BotId>
) {
    /** Score keeper */
    private val scoreTracker: ScoreTracker = ScoreTracker(participantIds)

    /** Map over all bots */
    private val botsMap: MutableMap<BotId, Bot> = HashMap()

    /** Map over all bot intents */
    private val botIntentsMap: MutableMap<BotId, BotIntent> = HashMap()

    /** Bullets */
    private val bullets: MutableSet<Bullet> = HashSet()

    /** Game state */
    private var gameState: GameState = GameState(Arena(setup.arenaWidth, setup.arenaHeight))

    /** Current round number */
    private var roundNumber: Int = 0

    /** Current turn number */
    var turnNumber: Int = 0
        private set

    /** Round record */
    private var round: Round = Round(roundNumber = roundNumber)

    /** Turn record */
    private var turn: Turn = Turn(turnNumber = turnNumber)

    /** Flag specifying if the round has ended */
    private var roundEnded = false

    /** Id for the next bullet that comes into existence */
    private var nextBulletId = 0

    /** Previous turn */
    private var previousTurn: Turn? = null

    /** Inactivity counter */
    private var inactivityCounter = 0

    /**
     * Updates game state.
     *
     * @param botIntents is the bot intents, which gives instructions to the game from the individual bot.
     * @return new game state.
     */
    fun update(botIntents: Map<BotId, BotIntent>): GameState {
        updateBotIntents(botIntents)
        if (roundNumber == 0 && turnNumber == 0 || roundEnded) {
            if (roundEnded) {
                calculatePlacements()
            }
            nextRound()
        }
        nextTurn()
        return updateGameState()
    }

    /** Calculates and sets placements for all bots, i.e. 1st, 2nd, and 3rd places. */
    fun calculatePlacements() {
        scoreTracker.calculatePlacements()
    }

    /** The current results ordered with highest total scores first. */
    val results: List<Score> get() = scoreTracker.results

    /** The number of rounds played so far. */
    val numberOfRounds: Int get() = gameState.rounds.size

    /**
     * Updates the current bot intents with the new bot intents.
     * @param botIntents is the new bot intents.
     */
    private fun updateBotIntents(botIntents: Map<BotId, BotIntent>) {
        for ((botId, updateIntent) in botIntents.entries) {
            val botIntent = botIntentsMap[botId] ?: BotIntent()
            botIntent.update(updateIntent)
            botIntentsMap[botId] = botIntent
        }
    }

    /** Proceed with next round. */
    private fun nextRound() {
        roundNumber++
        turnNumber = 0
        round = round.copy(roundNumber = roundNumber)
        roundEnded = false
        nextBulletId = 0
        bullets.clear()
        botsMap.clear()
        botIntentsMap.clear()
        scoreTracker.prepareRound()
        inactivityCounter = 0
        initializeBotStates()
    }

    /** Proceed with next turn. */
    private fun nextTurn() {
        previousTurn = round.lastTurn

        // Reset events
        turnNumber++
        turn.resetEvents()
        turn.turnNumber = turnNumber

        // Remove dead bots (cannot participate in new round)
        botsMap.values.removeIf(Bot::isDead)

        // Cool down and fire gun
        // Note: Called here before updating headings as we need to sync firing the gun with the gun's direction.
        // That is if the gun was set to fire with the last turn, then it will fire in the correct gun heading now.
        coolDownAndFireGuns()

        // Execute bot intents, which will update heading of body, gun and radar
        executeBotIntents()

        // Generate scan events
        generateScanEvents()

        // Check bot wall collisions
        checkBotWallCollisions()

        // Check bot to bot collisions
        checkBotCollisions()

        // Update bullet positions to new position
        updateBulletPositions()

        // Check bullet wall collisions
        checkBulletWallCollisions()

        // Check bullet hits
        checkBulletHits()

        // Check for inactivity
        checkInactivity()

        // Check for disabled bots
        checkForDisabledBots()

        // Cleanup defeated bots (events)
        checkForDefeatedBots()

        // Check if the round is over
        checkIfRoundOrGameOver()

        // Store bot and bullet snapshots
        turn.copyBots(botsMap.values)
        turn.copyBullets(bullets)
    }

    /**
     * Update game state.
     * @return new game state.
     */
    private fun updateGameState(): GameState {
        round = round.copy()
        round.turns += turn.copy()

        val roundIndex = round.roundNumber - 1
        val rounds = gameState.rounds
        if (rounds.size == roundIndex) {
            rounds += round
        } else {
            rounds[roundIndex] = round
        }
        return gameState
    }

    /** Initializes bot states */
    private fun initializeBotStates() {
        val occupiedCells: MutableSet<Int> = HashSet()
        for (id in participantIds) {
            val position = randomBotPosition(occupiedCells)
            val direction = randomDirection() // body, gun, and radar starts in the same direction
            botsMap[id] = Bot(
                id = id,
                position = position,
                direction = direction,
                gunDirection = direction,
                radarDirection = direction,
                score = Score(botId = id),
            )
        }
        // Store bot snapshots into the turn
        turn.copyBots(botsMap.values)
    }

    /**
     * Calculates a random bot position.
     * @param occupiedCells  is the occupied cells, where other bots are positioned.
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

    /** Execute bot intents for bots that are not disabled */
    private fun executeBotIntents() {
        for ((_, bot) in botsMap) {
            if (bot.isEnabled) executeBotIntent(bot)
        }
    }

    private fun executeBotIntent(bot: Bot) {
        val intent = botIntentsMap[bot.id]
        intent?.apply {
            bot.speed = calcNewBotSpeed(bot.speed, intent.targetSpeed ?: 0.0)

            updateBotTurnRatesAndDirections(bot, intent)
            updateBotColors(bot, intent)

            bot.moveToNewPosition()
        }
    }

    /** Update scan direction and scan spread */
    private fun updateScanDirectionAndSpread(bot: Bot, intent: BotIntent, newRadarDirection: Double) {
        // The radar sweep is the difference between the new and old radar direction
        val newSpreadAngle = normalRelativeDegrees(newRadarDirection - bot.radarDirection)
        val scan = intent.scan ?: false
        bot.scanDirection = if (scan) bot.radarDirection else newRadarDirection
        bot.scanSpreadAngle = if (scan) bot.radarSpreadAngle else newSpreadAngle
        bot.radarDirection = newRadarDirection
        bot.radarSpreadAngle = newSpreadAngle
    }

    /** Update bot turn rates and directions */
    private fun updateBotTurnRatesAndDirections(bot: Bot, intent: BotIntent) {
        val limitedTurnRate = limitTurnRate(intent.turnRate ?: 0.0, bot.speed)
        val limitedGunTurnRate = limitGunTurnRate(intent.gunTurnRate ?: 0.0)
        val limitedRadarTurnRate = limitRadarTurnRate(intent.radarTurnRate ?: 0.0)

        var totalTurnRate = limitedTurnRate
        bot.direction = normalAbsoluteDegrees(bot.direction + totalTurnRate)

        // Gun direction depends on the turn rate of both the body and the gun
        totalTurnRate += limitedGunTurnRate
        if (intent.adjustGunForBodyTurn == true) {
            totalTurnRate -= limitedTurnRate
        }
        bot.gunDirection = normalAbsoluteDegrees(bot.gunDirection + totalTurnRate)

        // Radar direction depends on the turn rate of the body, the gun, and the radar
        totalTurnRate += limitedRadarTurnRate
        if (intent.adjustRadarForGunTurn == true) {
            totalTurnRate -= limitedGunTurnRate
        }
        val radarDirection = normalAbsoluteDegrees(bot.radarDirection + totalTurnRate)

        updateScanDirectionAndSpread(bot, intent, radarDirection)

        bot.turnRate = limitedTurnRate
        bot.gunTurnRate = limitedGunTurnRate
        bot.radarTurnRate = limitedRadarTurnRate
    }

    /** Sets the bot colors */
    private fun updateBotColors(bot: Bot, intent: BotIntent) {
        bot.bodyColor = colorStringToRGB(intent.bodyColor)
        bot.turretColor = colorStringToRGB(intent.turretColor)
        bot.radarColor = colorStringToRGB(intent.radarColor)
        bot.bulletColor = colorStringToRGB(intent.bulletColor)
        bot.scanColor = colorStringToRGB(intent.scanColor)
        bot.tracksColor = colorStringToRGB(intent.tracksColor)
        bot.gunColor = colorStringToRGB(intent.gunColor)
    }

    /** Check bullet hits */
    private fun checkBulletHits() {
        if (bullets.size > 0) {
            // Two "arrays" that are both accessed with the same bullet index
            val bullets = ArrayList<Bullet>(bullets.size)
            val lines = ArrayList<Line>(bullets.size)

            for (bullet in this.bullets) {
                bullets += bullet
                // Create a line segment (from old to new point)
                lines += Line(bullet.calcPosition(), bullet.calcNextPosition())
            }
            checkBulletLineHits(bullets, lines)
        }
    }

    /** Check bullet line segments hit */
    private fun checkBulletLineHits(bullets: List<Bullet>, bulletLines: List<Line>) {
        val bulletCount = bullets.size

        for (i in 0 until bulletCount) {
            for (j in i + 1 until bulletCount) {
                checkBulletHitBots(bullets[i], bulletLines[i], bullets[j], bulletLines[j])
            }
            checkBulletHitBots(bullets[i], bulletLines[i])
        }
    }

    /** Checks if bullet hits bot */
    private fun checkBulletHitBots(bullet1: Bullet, bulletLine1: Line, bullet2: Bullet, bulletLine2: Line) {

        // Check if the bullets bounding circles intersects (is fast) before
        // checking if the bullets bounding lines intersect (is slower)
        if (isBulletsMaxBoundingCirclesColliding(bulletLine1.end, bulletLine2.end) &&
            isLineIntersectingLine(bulletLine1, bulletLine2)
        ) {
            val event1 = BulletHitBulletEvent(turnNumber, bullet1, bullet2)
            val event2 = BulletHitBulletEvent(turnNumber, bullet2, bullet1)

            turn.apply {
                addPrivateBotEvent(bullet1.botId, event1)
                addPrivateBotEvent(bullet2.botId, event2)
                // Observers only need a single event
                addObserverEvent(event1)
            }
            // Remove bullets from the arena
            bullets -= bullet1
            bullets -= bullet2
        }
    }

    /** Checks if bullet hits bot */
    private fun checkBulletHitBots(bullet: Bullet, bulletLine: Line) {
        // Check bullet-hit-bot collision (hit)
        for (bot in botsMap.values) {
            // A bot cannot shot itself
            if (bullet.botId != bot.id && isBulletHittingBot(bullet, bulletLine, bot)) {
                return
            }
        }
    }

    /** Checks if a bullet is hitting a bot */
    private fun isBulletHittingBot(bullet: Bullet, bulletLine: Line, bot: Bot): Boolean {
        val botId = bullet.botId
        val victimId = bot.id

        if (isLineIntersectingCircle(bulletLine, bot.position, BOT_BOUNDING_CIRCLE_RADIUS.toDouble())) {
            inactivityCounter = 0 // reset collective inactivity counter due to bot taking bullet damage

            val damage = calcBulletDamage(bullet.power)
            val isKilled = bot.addDamage(damage)

            val energyBonus = BULLET_HIT_ENERGY_GAIN_FACTOR * bullet.power
            botsMap[botId]?.changeEnergy(energyBonus)

            scoreTracker.registerBulletHit(botId, victimId, damage, isKilled)

            val bulletHitBotEvent = BulletHitBotEvent(turnNumber, bullet, victimId, damage, bot.energy)
            turn.apply {
                addPrivateBotEvent(botId, bulletHitBotEvent) // Bot itself gets event
                addPrivateBotEvent(victimId, bulletHitBotEvent) // Victim bot gets event too
                addObserverEvent(bulletHitBotEvent)
            }
            // Remove bullet from the arena
            bullets -= bullet
            return true
        }
        return false
    }

    /** Check collisions between bots */
    private fun checkBotCollisions() {
        val bots = ArrayList<Bot>(botsMap.values.size)
        botsMap.values.forEach { bots += it }

        for (i in 0 until bots.size) {
            for (j in i + 1 until bots.size) {
                if (isBotsColliding(bots[i], bots[j])) {
                    break
                }
            }
        }
    }

    private fun isBotsColliding(bot1: Bot, bot2: Bot): Boolean {
        if (isBotsBoundingCirclesColliding(bot1, bot2)) {

            val oldBot1Position = bot1.position
            val oldBot2Position = bot2.position

            val isBot1RammingBot2 = isRamming(bot1, bot2)
            val isBot2RammingBot1 = isRamming(bot2, bot1)

            // Both bots takes damage when hitting each other
            registerRamHit(bot1, bot2, isBot1RammingBot2, isBot2RammingBot1)

            val overlapDist = BOT_BOUNDING_CIRCLE_DIAMETER - distance(bot1.x, bot1.y, bot2.x, bot2.y)
            val totalSpeed = bot1.speed + bot2.speed
            val bot1BounceDist: Double
            val bot2BounceDist: Double
            if (totalSpeed == 0.0) {
                bot1BounceDist = overlapDist / 2
                bot2BounceDist = overlapDist / 2
            } else {
                val t = overlapDist / totalSpeed

                // The faster speed, the less bounce distance. Hence the speeds for the bots are swapped
                bot1BounceDist = bot2.speed * t
                bot2BounceDist = bot1.speed * t
            }
            bot1.bounceBack(bot1BounceDist)
            bot2.bounceBack(bot2BounceDist)

            val newBot1Position = bot1.position
            val newBot2Position = bot2.position

            // Check if one of the bot bounced into a wall
            if ((newBot1Position.x < BOT_BOUNDING_CIRCLE_RADIUS) || (newBot1Position.y < BOT_BOUNDING_CIRCLE_RADIUS) || (
                        newBot1Position.x > (setup.arenaWidth - BOT_BOUNDING_CIRCLE_RADIUS)) || (
                        newBot1Position.y > (setup.arenaHeight - BOT_BOUNDING_CIRCLE_RADIUS))
            ) {
                bot1.position = oldBot1Position
                bot2.bounceBack(bot1BounceDist /* remaining distance */)
            }
            if ((newBot2Position.x < BOT_BOUNDING_CIRCLE_RADIUS) || (newBot2Position.y < BOT_BOUNDING_CIRCLE_RADIUS) || (
                        newBot2Position.x > (setup.arenaWidth - BOT_BOUNDING_CIRCLE_RADIUS)) || (
                        newBot2Position.y > (setup.arenaHeight - BOT_BOUNDING_CIRCLE_RADIUS))
            ) {
                bot2.position = oldBot2Position
                bot1.bounceBack(bot2BounceDist /* remaining distance */)
            }
            if (isBot1RammingBot2) {
                bot1.speed = 0.0
            }
            if (isBot2RammingBot1) {
                bot2.speed = 0.0
            }
            val botHitBotEvent1 =
                BotHitBotEvent(turnNumber, bot1.id, bot2.id, bot2.energy, bot2.x, bot2.y, isBot1RammingBot2)
            val botHitBotEvent2 =
                BotHitBotEvent(turnNumber, bot2.id, bot1.id, bot1.energy, bot1.x, bot1.y, isBot2RammingBot1)
            turn.apply {
                addPrivateBotEvent(bot1.id, botHitBotEvent1)
                addPrivateBotEvent(bot2.id, botHitBotEvent2)
                addObserverEvent(botHitBotEvent1)
                addObserverEvent(botHitBotEvent2)
            }
            return true
        }
        return false
    }

    private fun registerRamHit(bot1: Bot, bot2: Bot, isBot1RammingBot2: Boolean, isBot2RammingBot1: Boolean) {
        // Both bots takes damage when hitting each other
        val bot1Killed = bot1.addDamage(RAM_DAMAGE)
        val bot2Killed = bot2.addDamage(RAM_DAMAGE)
        if (isBot1RammingBot2) {
            scoreTracker.registerRamHit(bot2.id, bot1.id, bot1Killed)
        }
        if (isBot2RammingBot1) {
            scoreTracker.registerRamHit(bot1.id, bot2.id, bot2Killed)
        }
    }

    /** Updates bullet positions */
    private fun updateBulletPositions() {
        bullets.forEach { it.incrementTick() }
    }

    /** Checks collisions between bots and the walls. */
    private fun checkBotWallCollisions() {
        for (bot in botsMap.values) {
            var x = bot.x
            var y = bot.y
            if (previousTurn != null) {
                val prevBotState: Bot = previousTurn!!.getBot(bot.id) ?: return
                val oldX = prevBotState.x
                val oldY = prevBotState.y
                val dx = x - oldX
                val dy = y - oldY
                var hitWall = false
                if (x - BOT_BOUNDING_CIRCLE_RADIUS < 0) {
                    hitWall = true
                    x = BOT_BOUNDING_CIRCLE_RADIUS.toDouble()
                    if (dx != 0.0) {
                        val dxCut = x - oldX
                        y = oldY + (dxCut * dy / dx)
                    }
                } else if (x + BOT_BOUNDING_CIRCLE_RADIUS > setup.arenaWidth) {
                    hitWall = true
                    x = setup.arenaWidth.toDouble() - BOT_BOUNDING_CIRCLE_RADIUS
                    if (dx != 0.0) {
                        val dxCut = x - oldX
                        y = oldY + (dxCut * dy / dx)
                    }
                } else if (y - BOT_BOUNDING_CIRCLE_RADIUS < 0) {
                    hitWall = true
                    y = BOT_BOUNDING_CIRCLE_RADIUS.toDouble()
                    if (dy != 0.0) {
                        val dyCut = y - oldY
                        x = oldX + (dyCut * dx / dy)
                    }
                } else if (y + BOT_BOUNDING_CIRCLE_RADIUS > setup.arenaHeight) {
                    hitWall = true
                    y = setup.arenaHeight.toDouble() - BOT_BOUNDING_CIRCLE_RADIUS
                    if (dy != 0.0) {
                        val dyCut = y - oldY
                        x = oldX + (dyCut * dx / dy)
                    }
                }
                if (hitWall) {
                    bot.x = x
                    bot.y = y

                    // Skip this check, if the bot hit the wall in the previous turn
                    if (previousTurn!!.getEvents(bot.id).none { event -> event is BotHitWallEvent }) {
                        val botHitWallEvent = BotHitWallEvent(turnNumber, bot.id)
                        turn.addPrivateBotEvent(bot.id, botHitWallEvent)
                        turn.addObserverEvent(botHitWallEvent)
                        val damage = calcWallDamage(bot.speed)
                        bot.addDamage(damage)
                    }

                    // Bot is stopped to zero speed regardless of its previous direction
                    bot.speed = 0.0
                }
            }
        }
    }

    /** Checks collisions between the bullets and the walls. */
    private fun checkBulletWallCollisions() {
        val iterator = bullets.iterator() // due to removal
        while (iterator.hasNext()) {
            val bullet = iterator.next()
            val position = bullet.calcPosition()
            if (((position.x <= 0) || (position.x >= setup.arenaWidth) ||
                        (position.y <= 0) || (position.y >= setup.arenaHeight))
            ) {
                iterator.remove() // remove bullet from arena
                val bulletHitWallEvent = BulletHitWallEvent(turnNumber, bullet)
                turn.addPrivateBotEvent(bullet.botId, bulletHitWallEvent)
                turn.addObserverEvent(bulletHitWallEvent)
            }
        }
    }

    /** Checks if the bots are inactive collectively. That is when no bot have been hit by bullets for some time. */
    private fun checkInactivity() {
        if (inactivityCounter++ > setup.maxInactivityTurns) {
            botsMap.values.forEach { it.addDamage(INACTIVITY_DAMAGE) }
        }
    }

    /** Check if the bots have been disabled (when energy is zero or close to zero). */
    private fun checkForDisabledBots() {
        for (bot in botsMap.values) {
            if (bot.energy < 0.01 && bot.energy > 0.0) {
                bot.energy = 0.0
            }
            // If bot is disabled => Set then reset bot movement with the bot intent
            if (bot.energy == 0.0) {
                botIntentsMap[bot.id]?.resetMovement()
            }
        }
    }

    /** Checks if any bots have been defeated. */
    private fun checkForDefeatedBots() {
        for (bot in botsMap.values) {
            if (bot.isDead) {
                val botDeathEvent = BotDeathEvent(turnNumber, bot.id)
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
            if (bot.gunHeat == 0.0 && bot.isEnabled) {
                // Gun can fire => Check if intent is set to fire gun
                val botIntent = botIntentsMap[bot.id]
                if (botIntent != null) {
                    val firepower = botIntent.bulletPower ?: 0.0
                    if (firepower >= MIN_FIREPOWER && bot.energy > firepower) {
                        fireBullet(bot, firepower)
                    }
                }
            } else {
                // Gun is too hot => Cool down gun
                bot.gunHeat = (bot.gunHeat - setup.gunCoolingRate).coerceAtLeast(0.0)
            }
        }
    }

    private fun fireBullet(bot: Bot, firepower: Double) {
        handleFiredBullet(bot, firepower.coerceAtMost(MAX_FIREPOWER))
    }

    /**
     * Handle fired bullet
     * @param bot is the bot firing the bullet.
     * @param firepower is the firepower of the bullet.
     */
    private fun handleFiredBullet(bot: Bot, firepower: Double) {
        bot.gunHeat = calcGunHeat(firepower)
        val bullet = Bullet(
            botId = bot.id,
            bulletId = BulletId(++nextBulletId),
            power = firepower,
            startX = bot.x,
            startY = bot.y,
            direction = bot.gunDirection,
            color = bot.bulletColor,
        )
        bullets += bullet
        val bulletFiredEvent = BulletFiredEvent(turnNumber, bullet)
        turn.addPrivateBotEvent(bot.id, bulletFiredEvent)
        turn.addObserverEvent(bulletFiredEvent)

        // Firing a bullet cost energy
        bot.changeEnergy(-firepower)
    }

    /** Checks the scan field for scanned bots. */
    private fun generateScanEvents() {
        val bots = ArrayList<Bot>(botsMap.values.size)
        botsMap.values.forEach { bots += it }

        for (i in 0 until bots.size) {
            val scanningBot = bots[i]
            val spreadAngle = scanningBot.scanSpreadAngle
            var arcStartAngle: Double
            var arcEndAngle: Double
            if (spreadAngle > 0) {
                arcEndAngle = scanningBot.scanDirection
                arcStartAngle = normalAbsoluteDegrees(arcEndAngle - spreadAngle)
            } else {
                arcStartAngle = scanningBot.scanDirection
                arcEndAngle = normalAbsoluteDegrees(arcStartAngle - spreadAngle)
            }
            for (j in 0 until bots.size) {
                if (i != j) {
                    val scannedBot = bots[j]
                    if (isCircleIntersectingCircleSector(
                            scannedBot.position, BOT_BOUNDING_CIRCLE_RADIUS.toDouble(),
                            scanningBot.position, RADAR_RADIUS,
                            arcStartAngle, arcEndAngle
                        )
                    ) {
                        val scannedBotEvent = ScannedBotEvent(
                            turnNumber, scanningBot.id, scannedBot.id, scannedBot.energy, scannedBot.x, scannedBot.y,
                            scannedBot.direction, scannedBot.speed
                        )
                        turn.addPrivateBotEvent(scanningBot.id, scannedBotEvent)
                        turn.addObserverEvent(scannedBotEvent)
                    }
                }
            }
        }
    }

    /** Checks if the round is ended or game is over. */
    private fun checkIfRoundOrGameOver() {
        if (botsMap.size <= 1) {
            roundEnded = true // Round ended
            if (roundNumber >= setup.numberOfRounds) {
                gameState.isGameEnded = true // Game over
            }
        }
    }

    companion object {
        /**
         * Checks if the maximum bounding circles of two bullets are colliding.
         * This is a pre-check if two bullets might be colliding.
         *
         * @param bullet1Position is the position of the 1st bullet.
         * @param bullet2Position is the position of the 2nd bullet.
         * @return `true` if the bounding circles are colliding; `false` otherwise.
         */
        private fun isBulletsMaxBoundingCirclesColliding(bullet1Position: Point, bullet2Position: Point): Boolean {
            val dx = bullet2Position.x - bullet1Position.x
            if (abs(dx) > BULLET_MAX_BOUNDING_CIRCLE_DIAMETER) {
                return false
            }
            val dy = bullet2Position.y - bullet1Position.y
            return abs(dy) <= BULLET_MAX_BOUNDING_CIRCLE_DIAMETER &&
                    ((dx * dx) + (dy * dy) <= BULLET_MAX_BOUNDING_CIRCLE_DIAMETER_SQUARED)
        }

        /**
         * Checks if the bounding circles of two bots are colliding.
         *
         * @param bot1 is one of the bots.
         * @param bot2 is another bot.
         * @return `true` if the bounding circles are colliding; `false` otherwise.
         */
        private fun isBotsBoundingCirclesColliding(bot1: Bot, bot2: Bot): Boolean {
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
         *
         * @param bot is the bot the attempts ramming.
         * @param victim is the victim bot.
         * @return `true` if the bot is ramming; `false` otherwise.
         */
        private fun isRamming(bot: Bot, victim: Bot): Boolean {
            val dx = victim.x - bot.x
            val dy = victim.y - bot.y
            val angle = atan2(dy, dx)
            val bearing = normalRelativeDegrees(Math.toDegrees(angle) - bot.direction)
            return (((bot.speed > 0 && (bearing > -90 && bearing < 90))
                    || (bot.speed < 0 && (bearing < -90 || bearing > 90))))
        }
    }
}