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

/**
 * Model updater, which is used for keeping track of the model state for each turn and round of a game.
 *
 * ## Threading contract
 * All public and internal methods on this class must be called exclusively while holding
 * `GameServer.tickLock`. The class itself performs no synchronization; thread safety is the
 * responsibility of the caller ([GameServer.onNextTurn]).
 */
class ModelUpdater(
    /** Game setup */
    private val setup: GameSetup,
    /** Participant ids */
    private val participantIds: Set<ParticipantId>,
    /** Initial positions */
    initialPositions: Map<BotId, InitialPosition>,
    /** Droid flags */
    droidFlags: Map<BotId, Boolean /* isDroid */>,
    /** Whether initial position overrides from bots are enabled */
    initialPositionEnabled: Boolean,
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

    /** Turn record — fresh instance created each turn to avoid temporal coupling */
    private var turn = MutableTurn(0)

    /** Counter to track the number of rounds played (memory leak fix) */
    private var roundCounter = 0

    /** Inactivity counter */
    private var inactivityCounter = 0

    /** Components */
    private val collisionDetector = CollisionDetector(setup, participantIds)
    private val botInitializer = BotInitializer(setup, participantIds, initialPositions, droidFlags, initialPositionEnabled)
    private val gunEngine = GunEngine(setup)

    private val turnProcessor = TurnProcessor(
        setup,
        gunEngine,
        collisionDetector,
        scoreTracker,
        scoreCalculator,
        participantIds
    )

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
    fun update(botIntents: Map<BotId, IBotIntent>): GameStateSnapshot {
        updateBotIntents(botIntents)
        if (round.roundEnded || (round.roundNumber == 0 && turn.turnNumber == 0)) {
            nextRound()
        }
        nextTurn()
        return updateGameState()
    }

    /**
     * Updates the game state.
     * @return new game state.
     */
    private fun updateGameState(): GameStateSnapshot {
        round.turns += turn.toTurn()

        // Memory leak fix: Keep only the last 2 turns (current + previous for collision detection)
        if (round.turns.size > 2) {
            round.turns.removeAt(0)
        }

        if (gameState.rounds.size == 0 || gameState.rounds.last().roundNumber != round.roundNumber) {
            gameState.rounds += round
        }
        return GameStateSnapshot(
            lastRound = gameState.lastRound,
            isGameEnded = gameState.isGameEnded,
        )
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
        round = MutableRound(round.roundNumber + 1)
        // Initialize to 0; nextTurn() will create a fresh MutableTurn(1) before the first TickEvent
        turn = MutableTurn(0)

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
        turn = MutableTurn(turn.turnNumber + 1)

        val result = turnProcessor.processTurn(
            turn,
            botsMap,
            botIntentsMap,
            botsCopies,
            round,
            bullets,
            inactivityCounter
        )

        inactivityCounter = result.inactivityCounter

        if (result.roundOutcome != null) {
            round.roundEnded = true
            if (result.roundOutcome.gameEnded) gameState.isGameEnded = true
            result.roundOutcome.winnerBotIds.forEach { botId ->
                turn.addPrivateBotEvent(botId, WonRoundEvent(turn.turnNumber))
            }
            accumulatedScoreCalculator.addScores(result.roundOutcome.scores)
        }
    }
}
