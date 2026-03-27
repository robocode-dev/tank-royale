package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.rules.*
import java.util.*

/** Initializer for bot states. */
class BotInitializer(
    private val setup: GameSetup,
    private val participantIds: Set<ParticipantId>,
    private val initialPositions: Map<BotId, InitialPosition>,
    private val droidFlags: Map<BotId, Boolean>
) {

    private val random = Random()

    fun initializeBotStates(botsMap: MutableMap<BotId, MutableBot>, turn: MutableTurn) {
        val occupiedCells = mutableSetOf<Int>()
        for (teamOrBotId in participantIds) {
            val botId = teamOrBotId.botId

            val isDroid = droidFlags[botId] ?: false
            val energy = if (isDroid) INITIAL_DROID_ENERGY else INITIAL_BOT_ENERGY

            val randomPosition = randomBotPosition(occupiedCells)
            val position = adjustForInitialPosition(botId, randomPosition)
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
                position = position,
                direction = direction,
                gunDirection = direction,
                radarDirection = direction,
            )
        }
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

    private fun randomBotPoint(
        occupiedCells: MutableSet<Int>,
        cellCount: Int,
        gridWidth: Int,
        cellWidth: Int,
        cellHeight: Int
    ): Point {
        while (true) {
            val cell = random.nextInt(cellCount)
            if (!occupiedCells.contains(cell)) {
                occupiedCells += cell
                var cellY = (cell / gridWidth).toDouble()
                var cellX = cell - cellY * gridWidth
                cellX *= cellWidth.toDouble()
                cellY *= cellHeight.toDouble()
                cellX += BOT_BOUNDING_CIRCLE_RADIUS + random.nextDouble() * (cellWidth - BOT_BOUNDING_CIRCLE_DIAMETER)
                cellY += BOT_BOUNDING_CIRCLE_RADIUS + random.nextDouble() * (cellHeight - BOT_BOUNDING_CIRCLE_DIAMETER)
                return Point(cellX, cellY)
            }
        }
    }
}
