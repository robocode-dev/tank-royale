package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.server.event.*
import dev.robocode.tankroyale.server.math.*
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.rules.*
import dev.robocode.tankroyale.server.score.ScoreTracker
import java.util.*
import java.util.function.Consumer
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/** Model updater, which keep track of the model state for each turn of a game. */
class ModelUpdater(
    /** Game setup */
    private val setup: GameSetup,
    /** Participant ids */
    private val participantIds: Set<BotId>
) {
    /** Score keeper */
    private val scoreTracker: ScoreTracker

    /** Map over all bots */
    private val botsMap: MutableMap<BotId, Bot> = HashMap()

    /** Map over all bot intents */
    private val botIntentsMap: MutableMap<BotId, BotIntent> = HashMap()

    /** Bullets */
    private val bullets: MutableSet<Bullet> = HashSet()

    /** Game state */
    private var gameState: GameState

    /** Round record */
    private var round: Round = Round(roundNumber = 0)

    /** Turn record */
    private var turn: Turn = Turn(turnNumber = 0)

    /** Current round number */
    private var roundNumber: Int

    /** Current turn number */
    var turnNumber: Int
        private set

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
    private fun calculatePlacements() {
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
        cooldownAndFireGuns()

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
        round.turns + turn.copy()

        val rounds = ArrayList(gameState.rounds)
        val roundIndex = round.roundNumber - 1
        if (rounds.size == roundIndex) {
            rounds + round
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
        for (id: BotId in participantIds) {
            val (x, y) = randomBotPosition(occupiedCells)
            val direction: Double = randomDirection() // body, gun, and radar starts in the same direction
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
        val boundingLines: Array<Line?> = arrayOfNulls(bullets.size)
        val bulletArray = bullets.toTypedArray()
        for (i in boundingLines.indices.reversed()) {
            val bullet = bulletArray[i]
            val line = Line(
                start = bullet.calcPosition(),
                end = bullet.calcNextPosition()
            )
            boundingLines[i] = line
        }
        for (i in boundingLines.indices.reversed()) {

            // Check bullet-bullet collision
            val line1: Line = boundingLines[i] ?: continue
            val endPos1: Point = line1.end
            for (j in i - 1 downTo 0) {
                val line2: Line = boundingLines[j] ?: continue
                val endPos2: Point = line2.end

                // Check if the bullets bounding circles intersects (is fast) before checking if the bullets bounding
                // lines intersect (is slower)
                if (isBulletsMaxBoundingCirclesColliding(endPos1, endPos2) &&
                    isLineIntersectingLine(
                        boundingLines[i]!!.start, boundingLines[i]!!.end,
                        boundingLines[j]!!.start, boundingLines[j]!!.end
                    )
                ) {
                    val bullet1 = bulletArray[i]
                    val bullet2 = bulletArray[j]
                    val event1 = BulletHitBulletEvent(turnNumber, bullet1, bullet2)
                    val event2 = BulletHitBulletEvent(turnNumber, bullet2, bullet1)
                    turn.addPrivateBotEvent(bullet1.botId, event1)
                    turn.addPrivateBotEvent(bullet2.botId, event2)

                    // Observers only need a single event
                    turn.addObserverEvent(event1)

                    // Remove bullets from the arena
                    bullets.remove(bulletArray[i])
                    bullets.remove(bulletArray[j])
                }
            }

            // Check bullet-hit-bot collision (hit)
            val startPos1: Point = boundingLines[i].start
            for (botBuilder: BotBuilder in botBuilderMap.values) {
                val botX: Double = botBuilder.getX()
                val botY: Double = botBuilder.getY()
                val bullet = bulletArray[i]
                val botId: Int = bullet!!.botId
                val victimId: Int = botBuilder.getId()
                if (botId == victimId) {
                    continue  // A bot cannot shot itself
                }
                if (MathUtil.isLineIntersectingCircle(
                        startPos1.x,
                        startPos1.y,
                        endPos1.x,
                        endPos1.y,
                        botX,
                        botY,
                        BOT_BOUNDING_CIRCLE_RADIUS
                    )
                ) {
                    inactivityCounter = 0 // reset collective inactivity counter due to bot taking bullet damage
                    val damage: Double = RuleMath.calcBulletDamage(bullet.power)
                    val killed: Boolean = botBuilder.addDamage(damage)
                    val energyBonus: Double = RuleConstants.BULLET_HIT_ENERGY_GAIN_FACTOR * bullet.power
                    val enemyBotBuilder: BotBuilder? = botBuilderMap[botId]
                    if (enemyBotBuilder != null) {
                        enemyBotBuilder.changeEnergy(energyBonus)
                    }
                    scoreTracker.registerBulletHit(botId, victimId, damage, killed)
                    val bulletHitBotEvent = BulletHitBotEvent(
                        turnNumber, bullet, victimId,
                        damage.toInt(), botBuilder.getEnergy()
                    )
                    turnBuilder.addPrivateBotEvent(botId, bulletHitBotEvent) // Bot itself gets event
                    turnBuilder.addPrivateBotEvent(victimId, bulletHitBotEvent) // Victim bot gets event too
                    turnBuilder.addObserverEvent(bulletHitBotEvent)

                    // Remove bullet from the arena
                    bullets.remove(bullet)
                }
            }
        }
    }

    /**
     * Check collisions between bots
     */
    private fun checkBotCollisions() {
        var botBuilderArray: Array<BotBuilder?> = arrayOfNulls<BotBuilder>(botBuilderMap.size)
        botBuilderArray = botBuilderMap.values.toArray(botBuilderArray)
        for (i in botBuilderArray.indices.reversed()) {
            val bot1x: Double = botBuilderArray[i].getX()
            val bot1y: Double = botBuilderArray[i].getY()
            for (j in i - 1 downTo 0) {
                val bot2x: Double = botBuilderArray[j].getX()
                val bot2y: Double = botBuilderArray[j].getY()
                if (isBotsBoundingCirclesColliding(bot1x, bot1y, bot2x, bot2y)) {
                    val overlapDist: Double =
                        BOT_BOUNDING_CIRCLE_DIAMETER - MathUtil.distance(bot1x, bot1y, bot2x, bot2y)
                    val botBuilder1: BotBuilder? = botBuilderArray[i]
                    val botBuilder2: BotBuilder? = botBuilderArray[j]
                    val botId1: Int = botBuilder1.getId()
                    val botId2: Int = botBuilder2.getId()

                    // Both bots takes damage when hitting each other
                    val bot1Killed: Boolean = botBuilder1.addDamage(RAM_DAMAGE)
                    val bot2Killed: Boolean = botBuilder2.addDamage(RAM_DAMAGE)
                    val bot1RammedBot2 = isRamming(botBuilder1, botBuilder2)
                    val bot2rammedBot1 = isRamming(botBuilder2, botBuilder1)
                    if (bot1RammedBot2) {
                        scoreTracker.registerRamHit(botId2, botId1, bot1Killed)
                    }
                    if (bot2rammedBot1) {
                        scoreTracker.registerRamHit(botId1, botId2, bot2Killed)
                    }
                    val totalSpeed: Double = botBuilder1.getSpeed() + botBuilder2.getSpeed()
                    var bot1BounceDist: Double
                    var bot2BounceDist: Double
                    if (totalSpeed == 0.0) {
                        bot1BounceDist = overlapDist / 2
                        bot2BounceDist = overlapDist / 2
                    } else {
                        val t = overlapDist / totalSpeed

                        // The faster speed, the less bounce distance. Hence the speeds for the bots are swapped
                        bot1BounceDist = botBuilder2.getSpeed() * t
                        bot2BounceDist = botBuilder1.getSpeed() * t
                    }
                    val oldBot1X: Double = botBuilder1.getX()
                    val oldBot1Y: Double = botBuilder1.getY()
                    val oldBot2X: Double = botBuilder2.getX()
                    val oldBot2Y: Double = botBuilder2.getY()
                    botBuilder1.bounceBack(bot1BounceDist)
                    botBuilder2.bounceBack(bot2BounceDist)
                    val newBot1X: Double = botBuilder1.getX()
                    val newBot1Y: Double = botBuilder1.getY()
                    val newBot2X: Double = botBuilder2.getX()
                    val newBot2Y: Double = botBuilder2.getY()

                    // Check if one of the bot bounced into a wall
                    if ((newBot1X < BOT_BOUNDING_CIRCLE_RADIUS) || (newBot1Y < BOT_BOUNDING_CIRCLE_RADIUS) || (
                                newBot1X > (setup.arenaWidth - BOT_BOUNDING_CIRCLE_RADIUS)) || (
                                newBot1Y > (setup.arenaHeight - BOT_BOUNDING_CIRCLE_RADIUS))
                    ) {
                        botBuilder1.x(oldBot1X)
                        botBuilder1.y(oldBot1Y)
                        botBuilder2.bounceBack(bot1BounceDist /* remaining distance */)

                        // FIXME: Add wall damage: abs(velocity) * 0.5 - 1 (never < 0).
                    }
                    if ((newBot2X < BOT_BOUNDING_CIRCLE_RADIUS) || (newBot2Y < BOT_BOUNDING_CIRCLE_RADIUS) || (
                                newBot2X > (setup.arenaWidth - BOT_BOUNDING_CIRCLE_RADIUS)) || (
                                newBot2Y > (setup.arenaHeight - BOT_BOUNDING_CIRCLE_RADIUS))
                    ) {
                        botBuilder2.x(oldBot2X)
                        botBuilder2.y(oldBot2Y)
                        botBuilder1.bounceBack(bot2BounceDist /* remaining distance */)

                        // FIXME: Add wall damage: abs(velocity) * 0.5 - 1 (never < 0).
                    }
                    if (bot1RammedBot2) {
                        botBuilder1.speed(0)
                    }
                    if (bot2rammedBot1) {
                        botBuilder2.speed(0)
                    }
                    val botHitBotEvent1 = BotHitBotEvent(
                        turnNumber,
                        botId1,
                        botId2,
                        botBuilder2.getEnergy(),
                        botBuilder2.getX(),
                        botBuilder2.getY(),
                        bot1RammedBot2
                    )
                    val botHitBotEvent2 = BotHitBotEvent(
                        turnNumber,
                        botId2,
                        botId1,
                        botBuilder1.getEnergy(),
                        botBuilder1.getX(),
                        botBuilder1.getY(),
                        bot2rammedBot1
                    )
                    turnBuilder.addPrivateBotEvent(botId1, botHitBotEvent1)
                    turnBuilder.addPrivateBotEvent(botId2, botHitBotEvent2)
                    turnBuilder.addObserverEvent(botHitBotEvent1)
                    turnBuilder.addObserverEvent(botHitBotEvent2)
                    break
                }
            }
        }
    }

    /**
     * Updates bullet positions
     */
    private fun updateBulletPositions() {
        val newBulletSet: MutableSet<Bullet?> = HashSet()
        for (bullet: Bullet? in bullets) {
            // The tick is used to calculate new position by calling getPosition()
            val updatedBullet: Bullet = bullet.toBuilder().tick(bullet!!.tick + 1).build()
            newBulletSet.add(updatedBullet)
        }
        bullets.clear()
        bullets.addAll(newBulletSet)
    }

    /**
     * Chekcs collisions between bots and the walls
     */
    private fun checkBotWallCollisions() {
        for (botBuilder: BotBuilder in botBuilderMap.values) {
            var x: Double = botBuilder.getX()
            var y: Double = botBuilder.getY()
            if (previousTurn != null) {
                val prevBotState: Bot = previousTurn.getBot(botBuilder.getId()) ?: continue
                val oldX = prevBotState.x
                val oldY = prevBotState.y
                val dx = x - oldX
                val dy = y - oldY
                var hitWall = false
                if (x - BOT_BOUNDING_CIRCLE_RADIUS < 0) {
                    hitWall = true
                    x = BOT_BOUNDING_CIRCLE_RADIUS
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
                    y = BOT_BOUNDING_CIRCLE_RADIUS
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
                    botBuilder.x(x)
                    botBuilder.y(y)

                    // Skip this check, if the bot hit the wall in the previous turn
                    if (previousTurn.getBotEvents(botBuilder.getId()).stream()
                            .noneMatch { e -> e is BotHitWallEvent }
                    ) {
                        val botHitWallEvent = BotHitWallEvent(turnNumber, botBuilder.getId())
                        turnBuilder.addPrivateBotEvent(botBuilder.getId(), botHitWallEvent)
                        turnBuilder.addObserverEvent(botHitWallEvent)
                        val damage: Double = RuleMath.calcWallDamage(botBuilder.getSpeed())
                        botBuilder.addDamage(damage)
                    }

                    // Bot is stopped to zero speed regardless of its previous direction
                    botBuilder.speed(0)
                }
            }
        }
    }

    /**
     * Checks collisions between the bullets and the walls
     */
    private fun checkBulletWallCollisions() {
        val iterator = bullets.iterator() // due to removal
        while (iterator.hasNext()) {
            val bullet = iterator.next()
            val position = bullet!!.calcPosition()
            if (((position.x <= 0) || (position.x >= setup.arenaWidth) || (position.y <= 0)
                        || (position.y >= setup.arenaHeight))
            ) {
                iterator.remove() // remove bullet from arena
                val bulletHitWallEvent = BulletHitWallEvent(turnNumber, bullet)
                turnBuilder.addPrivateBotEvent(bullet.botId, bulletHitWallEvent)
                turnBuilder.addObserverEvent(bulletHitWallEvent)
            }
        }
    }

    /**
     * Check if the bots are inactive collectively. That is when no bot have been hit by bullets for some time
     */
    private fun checkInactivity() {
        if (inactivityCounter++ > setup.inactivityTurns) {
            botBuilderMap.values.forEach(Consumer<BotBuilder> { bot: BotBuilder ->
                bot.addDamage(
                    INACTIVITY_DAMAGE
                )
            })
        }
    }

    /**
     * Check if the bots have been disabled (when energy is zero or close to zero)
     */
    private fun checkForDisabledBots() {
        botBuilderMap.values.forEach(Consumer<BotBuilder> { bot: BotBuilder ->
            if (bot.getEnergy() < 0.01 && bot.getEnergy() > 0) {
                bot.energy(0)
            }
            // If bot is disabled => Set then reset all bot intent values to zeros
            if (bot.getEnergy() === 0) {
                botIntentsMap.put(
                    bot.getId(),
                    BotIntent.builder().build().zeroed()
                )
            }
        })
    }

    /**
     * Checks if any bots have been defeated
     */
    private fun checkForDefeatedBots() {
        for (botBuilder: BotBuilder in botBuilderMap.values) {
            if (botBuilder.isDead()) {
                val victimId: Int = botBuilder.getId()
                val botDeathEvent = BotDeathEvent(turnNumber, victimId)
                turnBuilder.addPublicBotEvent(botDeathEvent)
                turnBuilder.addObserverEvent(botDeathEvent)
                scoreTracker.registerBotDeath(victimId)
            }
        }
    }

    /**
     * Cool down and fire guns
     */
    private fun cooldownAndFireGuns() {
        for (botBuilder: BotBuilder in botBuilderMap.values) {
            var gunHeat: Double = botBuilder.getGunHeat()

            // If gun heat is zero and the bot is enabled, it is able to fire
            if (gunHeat == 0.0 && botBuilder.isEnabled()) {
                // Gun can fire => Check if intent is set to fire gun
                val botIntent = botIntentsMap[botBuilder.getId()]
                if (botIntent != null) {
                    val firepower: Double = botIntent.nullsToZeros().getBulletPower()
                    if (firepower >= MIN_FIREPOWER && botBuilder.getEnergy() > firepower) {
                        fireBullet(botBuilder, firepower)
                    }
                }
            } else {
                // Gun is too hot => Cool down gun
                gunHeat = Math.max(gunHeat - setup.gunCoolingRate, 0.0)
                botBuilder.gunHeat(gunHeat)
            }
        }
    }

    private fun fireBullet(botBuilder: BotBuilder, firepower: Double) {
        // Gun is fired
        var firepower = firepower
        firepower = Math.min(firepower, MAX_FIREPOWER)
        handleFiredBullet(botBuilder, firepower)
    }

    /**
     * Handle fired bullet
     *
     * @param botBuilder
     * is the bot firing the bullet
     * @param firepower
     * is the firepower of the bullet
     */
    private fun handleFiredBullet(botBuilder: BotBuilder, firepower: Double) {
        val botId: Int = botBuilder.getId()
        val gunHeat: Double = RuleMath.calcGunHeat(firepower)
        botBuilder.gunHeat(gunHeat)
        val bullet: Bullet = Bullet.builder()
            .botId(botId)
            .bulletId(++nextBulletId)
            .power(firepower)
            .startX(botBuilder.getX())
            .startY(botBuilder.getY())
            .direction(botBuilder.getGunDirection())
            .color(botBuilder.getBulletColor())
            .build()
        bullets.add(bullet)
        val bulletFiredEvent = BulletFiredEvent(turnNumber, bullet)
        turnBuilder.addPrivateBotEvent(botId, bulletFiredEvent)
        turnBuilder.addObserverEvent(bulletFiredEvent)

        // Firing a bullet cost energy
        botBuilder.changeEnergy(-firepower)
    }

    /**
     * Checks the scan field for scanned bots
     */
    private fun generateScanEvents() {
        var botArray: Array<BotBuilder?> = arrayOfNulls<BotBuilder>(botBuilderMap.size)
        botArray = botBuilderMap.values.toArray(botArray)
        for (i in botArray.indices.reversed()) {
            val scanningBot: BotBuilder? = botArray[i]
            val spreadAngle: Double = scanningBot.getScanSpreadAngle()
            var arcStartAngle: Double
            var arcEndAngle: Double
            if (spreadAngle > 0) {
                arcEndAngle = scanningBot.getScanDirection()
                arcStartAngle = normalAbsoluteDegrees(arcEndAngle - spreadAngle)
            } else {
                arcStartAngle = scanningBot.getScanDirection()
                arcEndAngle = normalAbsoluteDegrees(arcStartAngle - spreadAngle)
            }
            for (j in botArray.indices.reversed()) {
                if (i != j) {
                    val scannedBot: BotBuilder? = botArray[j]
                    if (MathUtil.isCircleIntersectingCircleSector(
                            scannedBot.getX(), scannedBot.getY(),
                            BOT_BOUNDING_CIRCLE_RADIUS, scanningBot.getX(), scanningBot.getY(),
                            RuleConstants.RADAR_RADIUS, arcStartAngle, arcEndAngle
                        )
                    ) {
                        val scannedBotEvent = ScannedBotEvent(
                            turnNumber, scanningBot.getId(),
                            scannedBot.getId(), scannedBot.getEnergy(), scannedBot.getX(), scannedBot.getY(),
                            scannedBot.getDirection(), scannedBot.getSpeed()
                        )
                        turnBuilder.addPrivateBotEvent(scanningBot.getId(), scannedBotEvent)
                        turnBuilder.addObserverEvent(scannedBotEvent)
                    }
                }
            }
        }
    }

    /**
     * Checks if the round is ended or game is over
     */
    private fun checkIfRoundOrGameOver() {
        if (botBuilderMap.size < 2) {
            // Round ended
            roundEnded = true
            if (roundNumber >= setup.numberOfRounds) {
                // Game over
                gameState = gameState.toBuilder().gameEnded(true).build()
            }
        }
    }

    companion object {
        /**
         * Creates a new model updater
         *
         * @param setup
         * is the game setup
         * @param participantIds
         * is the ids of the participating bots
         * @return model updater
         */
        fun create(setup: GameSetup, participantIds: Set<Int>): ModelUpdater {
            return ModelUpdater(setup, participantIds)
        }

        /** Maximum bounding circle diameter of a bullet moving with max speed  */
        private val BULLET_MAX_BOUNDING_CIRCLE_DIAMETER: Double = 2 * MAX_BULLET_SPEED

        /** Square of maximum bounding circle diameter of a bullet moving with max speed  */
        private val BULLET_MAX_BOUNDING_CIRCLE_DIAMETER_SQUARED = (BULLET_MAX_BOUNDING_CIRCLE_DIAMETER
                * BULLET_MAX_BOUNDING_CIRCLE_DIAMETER)

        /**
         * Checks if the maximum bounding circles of two bullets are colliding. This is a pre-check if two bullets might be
         * colliding.
         *
         * @param bullet1Position
         * is the position of the 1st bullet
         * @param bullet2Position
         * is the position of the 2nd bullet
         * @return true if the bounding circles are colliding; false otherwise
         */
        private fun isBulletsMaxBoundingCirclesColliding(bullet1Position: Point, bullet2Position: Point): Boolean {
            val dx = bullet2Position.x - bullet1Position.x
            if (Math.abs(dx) > BULLET_MAX_BOUNDING_CIRCLE_DIAMETER) {
                return false
            }
            val dy = bullet2Position.y - bullet1Position.y
            return Math.abs(dy) <= BULLET_MAX_BOUNDING_CIRCLE_DIAMETER && ((dx * dx) + (dy * dy) <= BULLET_MAX_BOUNDING_CIRCLE_DIAMETER_SQUARED)
        }

        /** Square of the bounding circle diameter of a bot  */
        private val BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED: Double = (BOT_BOUNDING_CIRCLE_DIAMETER as Double
                * BOT_BOUNDING_CIRCLE_DIAMETER)

        /**
         * Checks if the bounding circles of two bots are colliding.
         *
         * @param bot1x
         * is the x coordinate of the 1st bot
         * @param bot1y
         * is the y coordinate of the 1st bot
         * @param bot2x
         * is the x coordinate of the 2nd bot
         * @param bot2y
         * is the y coordinate of the 2nd bot
         * @return true if the bounding circles are colliding; false otherwise
         */
        private fun isBotsBoundingCirclesColliding(
            bot1x: Double,
            bot1y: Double,
            bot2x: Double,
            bot2y: Double
        ): Boolean {
            val dx = bot2x - bot1x
            if (Math.abs(dx) > BOT_BOUNDING_CIRCLE_DIAMETER) { // 2 x radius
                return false
            }
            val dy = bot2y - bot1y
            // 2 x radius
            return Math.abs(dy) <= BOT_BOUNDING_CIRCLE_DIAMETER && ((dx * dx) + (dy * dy) <= BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED)
        }

        /**
         * Checks if a bot is ramming another bot
         *
         * @param bot
         * is the bot the attempts ramming
         * @param victim
         * is the victim bot
         * @return true if the bot is ramming; false otherwise
         */
        private fun isRamming(bot: BotBuilder?, victim: BotBuilder?): Boolean {
            val dx: Double = victim.getX() - bot.getX()
            val dy: Double = victim.getY() - bot.getY()
            val angle = Math.atan2(dy, dx)
            val bearing: Double = MathUtil.normalRelativeDegrees(Math.toDegrees(angle) - bot.getDirection())
            return (((bot.getSpeed() > 0 && (bearing > -90 && bearing < 90))
                    || (bot.getSpeed() < 0 && (bearing < -90 || bearing > 90))))
        }
    }

    /**
     * Creates a new model updater
     *
     * @param setup
     * is the game setup
     * @param participantIds
     * is the ids of the participating bots
     */
    init {
        this.participantIds = HashSet(participantIds)
        scoreTracker = ScoreTracker(participantIds)
        round = Round.builder().build()
        turnBuilder = Turn.builder()

        // Prepare game state builder
        val arena = Arena(
            Size(
                setup.arenaWidth.toDouble(),
                setup.arenaHeight.toDouble()
            )
        )
        gameState = GameState.builder().arena(arena).build()
        roundNumber = 0
        turnNumber = 0
    }
}
