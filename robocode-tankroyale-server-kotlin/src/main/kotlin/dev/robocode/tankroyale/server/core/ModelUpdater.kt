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
     * Updates game state
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
    val results: List<Score>
        get() = scoreTracker.results

    /** The number of rounds played so far. */
    val numberOfRounds: Int
        get() = gameState.rounds.size

    /**
     * Updates the current bot intents with the new bot intents.
     * @param botIntents is the new bot intents.
     */
    private fun updateBotIntents(botIntents: Map<BotId, BotIntent>) {
        botIntents.entries.forEach { (botId, updateIntent) ->
            run {
                val botIntent = botIntentsMap[botId] ?: BotIntent()
                botIntent.update(updateIntent)
                botIntentsMap[botId] = botIntent
            }
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
        initializeBotStates()
        scoreTracker.prepareRound()
        inactivityCounter = 0
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

        // Store bot snapshots
        turn.bots.apply {
            clear()
            botsMap.values.forEach { add(it.copy()) }
        }

        // Store bot snapshots into the turn
        turn.bullets.apply {
            clear()
            bullets.forEach { add(it.copy()) }
        }
    }

    /**
     * Update game state.
     * @return new game state.
     */
    private fun updateGameState(): GameState {
        round = round.copy()
        round.turns.add(turn.copy())

        val rounds = ArrayList(gameState.rounds)
        val roundIndex = round.roundNumber - 1
        if (rounds.size == roundIndex) {
            rounds.add(round)
        } else {
            rounds[roundIndex] = round
        }
        gameState = gameState.copy()
        gameState.rounds.apply {
            clear()
            addAll(rounds)
        }
        return gameState
    }

    /** Initializes bot states */
    private fun initializeBotStates() {
        val occupiedCells: MutableSet<Int> = HashSet()
        for (id in participantIds) {
            val (x, y) = randomBotPosition(occupiedCells)
            val direction = randomDirection() // body, gun, and radar starts in the same direction
            botsMap[id] = Bot(
                id = id,
                x = x,
                y = y,
                direction = direction,
                gunDirection = direction,
                radarDirection = direction,
                score = Score(botId = id),
            )
        }

        // Store bot snapshots into the turn
        turn.bots.apply {
            clear()
            botsMap.values.forEach { add(it.copy()) }
        }
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
        var x: Double
        var y: Double
        val random = Random()
        while (true) {
            val cell = random.nextInt(cellCount)
            if (!occupiedCells.contains(cell)) {
                occupiedCells.add(cell)
                y = (cell / gridWidth).toDouble()
                x = cell - y * gridWidth
                x *= cellWidth.toDouble()
                y *= cellHeight.toDouble()
                x += BOT_BOUNDING_CIRCLE_RADIUS + Math.random() * (cellWidth - BOT_BOUNDING_CIRCLE_DIAMETER)
                y += BOT_BOUNDING_CIRCLE_RADIUS + Math.random() * (cellHeight - BOT_BOUNDING_CIRCLE_DIAMETER)
                break
            }
        }
        return Point(x, y)
    }

    /** Execute bot intents for bots that are not disabled */
    private fun executeBotIntents() {
        botsMap.forEach { (_, bot) -> if (bot.isEnabled) executeBotIntent(bot) }
    }

    private fun executeBotIntent(bot: Bot) {
        val intent = botIntentsMap[bot.id]
        intent?.apply {
            val speed = calcNewBotSpeed(bot.speed, intent.targetSpeed ?: 0.0)
            val limitedTurnRate = limitTurnRate(intent.turnRate ?: 0.0, speed)
            val limitedGunTurnRate = limitGunTurnRate(intent.gunTurnRate ?: 0.0)
            val limitedRadarTurnRate = limitRadarTurnRate(intent.radarTurnRate ?: 0.0)

            var totalTurnRate = limitedTurnRate
            val direction: Double = normalAbsoluteDegrees(bot.direction + totalTurnRate)

            // Gun direction depends on the turn rate of both the body and the gun
            totalTurnRate += limitedGunTurnRate
            if (intent.adjustGunForBodyTurn == true) {
                totalTurnRate -= limitedTurnRate
            }
            val gunDirection = normalAbsoluteDegrees(bot.gunDirection + totalTurnRate)

            // Radar direction depends on the turn rate of the body, the gun, and the radar
            totalTurnRate += limitedRadarTurnRate
            if (intent.adjustRadarForGunTurn == true) {
                totalTurnRate -= limitedGunTurnRate
            }
            val radarDirection: Double = normalAbsoluteDegrees(bot.radarDirection + totalTurnRate)

            // The radar sweep is the difference between the new and old radar direction
            val spreadAngle = normalRelativeDegrees(radarDirection - bot.radarDirection)
            val scan = intent.scan ?: false
            bot.scanDirection = if (scan) bot.radarDirection else radarDirection
            bot.scanSpreadAngle = if (scan) bot.radarSpreadAngle else spreadAngle

            bot.direction = direction
            bot.gunDirection = gunDirection
            bot.radarDirection = radarDirection
            bot.radarSpreadAngle = spreadAngle
            bot.speed = speed
            bot.turnRate = limitedTurnRate
            bot.gunTurnRate = limitedGunTurnRate
            bot.radarTurnRate = limitedRadarTurnRate
            bot.moveToNewPosition()
            bot.bodyColor = colorStringToRGB(intent.bodyColor)
            bot.turretColor = colorStringToRGB(intent.turretColor)
            bot.radarColor = colorStringToRGB(intent.radarColor)
            bot.bulletColor = colorStringToRGB(intent.bulletColor)
            bot.scanColor = colorStringToRGB(intent.scanColor)
            bot.tracksColor = colorStringToRGB(intent.tracksColor)
            bot.gunColor = colorStringToRGB(intent.gunColor)
        }
    }

    /** Check bullet hits */
    private fun checkBulletHits() {
        val bulletCount = this.bullets.size.also { size ->
            if (size == 0) return
        }

        // Two "arrays" that are both accessed with the same bullet index
        val bullets = ArrayList<Bullet>(bulletCount)
        val lines = ArrayList<Line>(bulletCount)

        this.bullets.forEach { bullet ->
            bullets.add(bullet)
            // Create a line segment (from old to new point)
            lines.add(Line(bullet.calcPosition(), bullet.calcNextPosition()))
        }

        for (i in 0 until bulletCount) {
            for (j in 1 until bulletCount) {
                // Check if the bullets bounding circles intersects (is fast) before checking if the bullets bounding
                // lines intersect (is slower)
                if (isBulletsMaxBoundingCirclesColliding(lines[i].end, lines[j].end) &&
                    isLineIntersectingLine(lines[i], lines[j])
                ) {

                    val event1 = BulletHitBulletEvent(turnNumber, bullets[i], bullets[j])
                    val event2 = BulletHitBulletEvent(turnNumber, bullets[j], bullets[i])
                    turn.addPrivateBotEvent(bullets[i].botId, event1)
                    turn.addPrivateBotEvent(bullets[j].botId, event2)

                    // Observers only need a single event
                    turn.addObserverEvent(event1)

                    // Remove bullets from the arena
                    this.bullets.remove(bullets[i])
                    this.bullets.remove(bullets[j])
                }
            }

            // Check bullet-hit-bot collision (hit)
            botsMap.values.forEach { bot ->
                run {
                    val bullet = bullets[i]
                    val botId = bullet.botId
                    val victimId = bot.id
                    if (botId == victimId) {
                        return // A bot cannot shot itself
                    }
                    if (isLineIntersectingCircle(
                            lines[i],
                            Point(bot.x, bot.y),
                            BOT_BOUNDING_CIRCLE_RADIUS.toDouble()
                        )
                    ) {
                        inactivityCounter = 0 // reset collective inactivity counter due to bot taking bullet damage
                        val damage = calcBulletDamage(bullet.power)
                        val isKilled = bot.addDamage(damage)
                        val energyBonus = BULLET_HIT_ENERGY_GAIN_FACTOR * bullet.power
                        val enemyBot = botsMap[botId]
                        enemyBot?.changeEnergy(energyBonus)
                        scoreTracker.registerBulletHit(botId, victimId, damage, isKilled)
                        val bulletHitBotEvent = BulletHitBotEvent(turnNumber, bullet, victimId, damage, bot.energy)
                        turn.addPrivateBotEvent(botId, bulletHitBotEvent) // Bot itself gets event
                        turn.addPrivateBotEvent(victimId, bulletHitBotEvent) // Victim bot gets event too
                        turn.addObserverEvent(bulletHitBotEvent)

                        // Remove bullet from the arena
                        this.bullets.remove(bullet)
                    }
                }
            }
        }
    }

    /** Check collisions between bots */
    private fun checkBotCollisions() {
        val botArray = botsMap.values.toTypedArray()
        for (i in botArray.indices.reversed()) {
            val bot1x = botArray[i].x
            val bot1y = botArray[i].y
            for (j in i - 1 downTo 0) {
                val bot2x = botArray[j].x
                val bot2y = botArray[j].y
                if (isBotsBoundingCirclesColliding(bot1x, bot1y, bot2x, bot2y)) {
                    val overlapDist = BOT_BOUNDING_CIRCLE_DIAMETER - distance(bot1x, bot1y, bot2x, bot2y)
                    val bot1 = botArray[i]
                    val bot2 = botArray[j]
                    val botId1 = bot1.id
                    val botId2 = bot2.id

                    // Both bots takes damage when hitting each other
                    val bot1Killed = bot1.addDamage(RAM_DAMAGE)
                    val bot2Killed = bot2.addDamage(RAM_DAMAGE)
                    val isBot1RammingBot2 = isRamming(bot1, bot2)
                    val isBot2RammingBot1 = isRamming(bot2, bot1)
                    if (isBot1RammingBot2) {
                        scoreTracker.registerRamHit(botId2, botId1, bot1Killed)
                    }
                    if (isBot2RammingBot1) {
                        scoreTracker.registerRamHit(botId1, botId2, bot2Killed)
                    }
                    val totalSpeed = bot1.speed + bot2.speed
                    var bot1BounceDist: Double
                    var bot2BounceDist: Double
                    if (totalSpeed == 0.0) {
                        bot1BounceDist = overlapDist / 2
                        bot2BounceDist = overlapDist / 2
                    } else {
                        val t = overlapDist / totalSpeed

                        // The faster speed, the less bounce distance. Hence the speeds for the bots are swapped
                        bot1BounceDist = bot2.speed * t
                        bot2BounceDist = bot1.speed * t
                    }
                    val oldBot1X = bot1.x
                    val oldBot1Y = bot1.y
                    val oldBot2X = bot2.x
                    val oldBot2Y = bot2.y
                    bot1.bounceBack(bot1BounceDist)
                    bot2.bounceBack(bot2BounceDist)
                    val newBot1X = bot1.x
                    val newBot1Y = bot1.y
                    val newBot2X = bot2.x
                    val newBot2Y = bot2.y

                    // Check if one of the bot bounced into a wall
                    if ((newBot1X < BOT_BOUNDING_CIRCLE_RADIUS) || (newBot1Y < BOT_BOUNDING_CIRCLE_RADIUS) || (
                                newBot1X > (setup.arenaWidth - BOT_BOUNDING_CIRCLE_RADIUS)) || (
                                newBot1Y > (setup.arenaHeight - BOT_BOUNDING_CIRCLE_RADIUS))
                    ) {
                        bot1.x = oldBot1X
                        bot1.y = oldBot1Y
                        bot2.bounceBack(bot1BounceDist /* remaining distance */)

                        // FIXME: Add wall damage: abs(velocity) * 0.5 - 1 (never < 0).
                    }
                    if ((newBot2X < BOT_BOUNDING_CIRCLE_RADIUS) || (newBot2Y < BOT_BOUNDING_CIRCLE_RADIUS) || (
                                newBot2X > (setup.arenaWidth - BOT_BOUNDING_CIRCLE_RADIUS)) || (
                                newBot2Y > (setup.arenaHeight - BOT_BOUNDING_CIRCLE_RADIUS))
                    ) {
                        bot2.x = oldBot2X
                        bot2.y = oldBot2Y
                        bot1.bounceBack(bot2BounceDist /* remaining distance */)

                        // FIXME: Add wall damage: abs(velocity) * 0.5 - 1 (never < 0).
                    }
                    if (isBot1RammingBot2) {
                        bot1.speed = 0.0
                    }
                    if (isBot2RammingBot1) {
                        bot2.speed = 0.0
                    }
                    val botHitBotEvent1 = BotHitBotEvent(
                        turnNumber,
                        botId1,
                        botId2,
                        bot2.energy,
                        bot2.x,
                        bot2.y,
                        isBot1RammingBot2
                    )
                    val botHitBotEvent2 = BotHitBotEvent(
                        turnNumber,
                        botId2,
                        botId1,
                        bot1.energy,
                        bot1.x,
                        bot1.y,
                        isBot2RammingBot1
                    )
                    turn.addPrivateBotEvent(botId1, botHitBotEvent1)
                    turn.addPrivateBotEvent(botId2, botHitBotEvent2)
                    turn.addObserverEvent(botHitBotEvent1)
                    turn.addObserverEvent(botHitBotEvent2)
                    break
                }
            }
        }
    }

    /** Updates bullet positions */
    private fun updateBulletPositions() {
        bullets.forEach { it.incrementTick() }
    }

    /** Checks collisions between bots and the walls. */
    private fun checkBotWallCollisions() {
        botsMap.values.forEach { bot ->
            run {
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
        botsMap.values.forEach { bot ->
            run {
                if (bot.energy < 0.01 && bot.energy > 0.0) {
                    bot.energy = 0.0
                }
            }
            // If bot is disabled => Set then reset bot movement with the bot intent
            if (bot.energy == 0.0) {
                botIntentsMap[bot.id]?.resetMovement()
            }
        }
    }

    /** Checks if any bots have been defeated. */
    private fun checkForDefeatedBots() {
        botsMap.values.forEach { bot ->
            run {
                if (bot.isDead) {
                    val botDeathEvent = BotDeathEvent(turnNumber, bot.id)
                    turn.addPublicBotEvent(botDeathEvent)
                    turn.addObserverEvent(botDeathEvent)
                    scoreTracker.registerBotDeath(bot.id)
                }
            }
        }
    }

    /** Cool down and fire guns. */
    private fun coolDownAndFireGuns() {
        botsMap.values.forEach { bot ->
            run {
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
            bulletId = ++nextBulletId,
            power = firepower,
            startX = bot.x,
            startY = bot.y,
            direction = bot.gunDirection,
            color = bot.bulletColor,
        )
        bullets.add(bullet)
        val bulletFiredEvent = BulletFiredEvent(turnNumber, bullet)
        turn.addPrivateBotEvent(bot.id, bulletFiredEvent)
        turn.addObserverEvent(bulletFiredEvent)

        // Firing a bullet cost energy
        bot.changeEnergy(-firepower)
    }

    /** Checks the scan field for scanned bots. */
    private fun generateScanEvents() {
        val botArray = botsMap.values.toTypedArray()
        for (i in botArray.indices.reversed()) {
            val scanningBot = botArray[i]
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
            for (j in botArray.indices.reversed()) {
                if (i != j) {
                    val scannedBot = botArray[j]
                    if (isCircleIntersectingCircleSector(
                            Point(scannedBot.x, scannedBot.y), BOT_BOUNDING_CIRCLE_RADIUS.toDouble(),
                            Point(scanningBot.x, scanningBot.y), RADAR_RADIUS,
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
         * @param bot1x is the x coordinate of the 1st bot.
         * @param bot1y is the y coordinate of the 1st bot.
         * @param bot2x is the x coordinate of the 2nd bot.
         * @param bot2y is the y coordinate of the 2nd bot.
         * @return `true` if the bounding circles are colliding; `false` otherwise.
         */
        private fun isBotsBoundingCirclesColliding(
            bot1x: Double,
            bot1y: Double,
            bot2x: Double,
            bot2y: Double
        ): Boolean {
            val dx = bot2x - bot1x
            if (abs(dx) > BOT_BOUNDING_CIRCLE_DIAMETER) { // 2 x radius
                return false
            }
            val dy = bot2y - bot1y
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