package dev.robocode.tankroyale.runner

import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.common.event.Event
import dev.robocode.tankroyale.runner.internal.ServerConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Handle for an in-progress asynchronous battle.
 *
 * Provides typed event access for real-time observation and control methods for
 * battle management. Obtain via [BattleRunner.startBattleAsync].
 *
 * Must be [closed][close] when the battle is done to release bot processes and
 * allow subsequent battles on the same [BattleRunner].
 */
class BattleHandle internal constructor(
    private val connection: ServerConnection,
    private val onClose: () -> Unit,
) : AutoCloseable {

    private val owner = Any()
    private val gameEndedLatch = CountDownLatch(1)
    private val resultsRef = AtomicReference<BattleResults?>()
    private val errorRef = AtomicReference<BattleException?>()

    init {
        connection.onGameEnded.on(owner) { event ->
            resultsRef.set(ServerConnection.toBattleResults(event))
            gameEndedLatch.countDown()
        }
        connection.onGameAborted.on(owner) { _ ->
            errorRef.set(BattleException("Battle was aborted"))
            gameEndedLatch.countDown()
        }
    }

    // -------------------------------------------------------------------------------------
    // Events — delegate to the underlying connection for real-time observation
    // -------------------------------------------------------------------------------------

    /** Fires on each game tick with full observer state. */
    val onTickEvent: Event<TickEvent> get() = connection.onTickEvent

    /** Fires when a new round begins. */
    val onRoundStarted: Event<RoundStartedEvent> get() = connection.onRoundStarted

    /** Fires when a round ends. */
    val onRoundEnded: Event<RoundEndedEvent> get() = connection.onRoundEnded

    /** Fires when the game starts (all bots ready). */
    val onGameStarted: Event<GameStartedEvent> get() = connection.onGameStarted

    /** Fires when the game ends with final results. */
    val onGameEnded: Event<GameEndedEvent> get() = connection.onGameEnded

    /** Fires when the game is aborted. */
    val onGameAborted: Event<GameAbortedEvent> get() = connection.onGameAborted

    /** Fires when the game is paused. */
    val onGamePaused: Event<GamePausedEvent> get() = connection.onGamePaused

    /** Fires when the game resumes from pause. */
    val onGameResumed: Event<GameResumedEvent> get() = connection.onGameResumed

    /** Fires when the connected bot list changes. */
    val onBotListUpdate: Event<BotListUpdate> get() = connection.onBotListUpdate

    /**
     * Fires periodically (every 500 ms) and on every [BotListUpdate] during the boot phase,
     * reporting how many bots have connected versus how many are expected.
     */
    val onBootProgress: Event<BootProgress> = Event()

    // -------------------------------------------------------------------------------------
    // Control
    // -------------------------------------------------------------------------------------

    /** Pauses the battle. */
    fun pause() = connection.pauseBattle()

    /** Resumes a paused battle. */
    fun resume() = connection.resumeBattle()

    /** Stops the battle. */
    fun stop() = connection.stopBattle()

    /** Advances one turn while paused (single-step debugging). */
    fun nextTurn() = connection.nextTurn()

    // -------------------------------------------------------------------------------------
    // Await completion
    // -------------------------------------------------------------------------------------

    /**
     * Blocks until the battle completes and returns structured results.
     *
     * @return per-bot scores and rankings
     * @throws BattleException if the battle is aborted or the connection is lost
     */
    fun awaitResults(): BattleResults {
        while (gameEndedLatch.count > 0) {
            if (gameEndedLatch.await(1, TimeUnit.SECONDS)) break
            if (!connection.isConnected) {
                throw BattleException("Lost connection to server during battle")
            }
        }
        errorRef.get()?.let { throw it }
        return resultsRef.get() ?: throw BattleException("Battle completed without results")
    }

    /** Unsubscribes internal event handlers and releases the battle slot. */
    override fun close() {
        connection.onGameEnded.off(owner)
        connection.onGameAborted.off(owner)
        onClose()
    }
}
