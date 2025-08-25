package dev.robocode.tankroyale.gui.player

import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.common.Event

/**
 * Manages the lifecycle and coordination of battle players.
 * Acts as a central coordinator between the UI and different battle player implementations.
 */
object BattleManager {

    private var currentPlayer: BattlePlayer? = null

    /**
     * Gets the currently active battle player, if any.
     */
    fun getCurrentPlayer(): BattlePlayer? = currentPlayer

    /**
     * Registers and activates a battle player.
     * Any previously active player will be stopped.
     */
    fun setPlayer(player: BattlePlayer) {
        // Stop current player if exists
        currentPlayer?.let { current ->
            if (current.isRunning()) {
                current.stop()
            }
            unsubscribeFromPlayerEvents(current)
        }

        currentPlayer = player
        subscribeToPlayerEvents(player)
    }

    /**
     * Clears the current battle player.
     */
    fun clearPlayer() {
        currentPlayer?.let { player ->
            if (player.isRunning()) {
                player.stop()
            }
            unsubscribeFromPlayerEvents(player)
        }
        currentPlayer = null
    }

    /**
     * Returns the features supported by the current player.
     */
    fun getSupportedFeatures(): Set<BattlePlayerFeature> {
        return currentPlayer?.getSupportedFeatures() ?: emptySet()
    }

    /**
     * Checks if a specific feature is supported by the current player.
     */
    fun supportsFeature(feature: BattlePlayerFeature): Boolean {
        return currentPlayer?.supportsFeature(feature) ?: false
    }

    // Delegate methods to current player

    fun start() {
        currentPlayer?.start() ?: throw IllegalStateException("No active battle player")
    }

    fun stop() {
        currentPlayer?.stop()
    }

    fun pause() {
        currentPlayer?.pause() ?: throw IllegalStateException("No active battle player")
    }

    fun resume() {
        currentPlayer?.resume() ?: throw IllegalStateException("No active battle player")
    }

    fun nextTurn() {
        currentPlayer?.nextTurn() ?: throw IllegalStateException("No active battle player")
    }

    fun restart() {
        currentPlayer?.restart() ?: throw IllegalStateException("No active battle player")
    }

    fun seekToTurn(turnNumber: Int) {
        currentPlayer?.seekToTurn(turnNumber) ?: throw IllegalStateException("No active battle player")
    }

    fun isRunning(): Boolean {
        return currentPlayer?.isRunning() ?: false
    }

    fun isPaused(): Boolean {
        return currentPlayer?.isPaused() ?: false
    }

    fun getCurrentGameSetup(): GameSetup? {
        return currentPlayer?.getCurrentGameSetup()
    }

    fun getCurrentTick(): TickEvent? {
        return currentPlayer?.getCurrentTick()
    }

    // Events forwarded from current player

    val onConnected = Event<Unit>()
    val onGameStarted = Event<GameStartedEvent>()
    val onGameEnded = Event<GameEndedEvent>()
    val onGameAborted = Event<GameAbortedEvent>()
    val onGamePaused = Event<GamePausedEvent>()
    val onGameResumed = Event<GameResumedEvent>()
    val onRoundStarted = Event<RoundStartedEvent>()
    val onRoundEnded = Event<RoundEndedEvent>()
    val onTickEvent = Event<TickEvent>()
    val onBotListUpdate = Event<BotListUpdate>()
    val onStdOutputUpdated = Event<TickEvent>()
    val onPlayerChanged = Event<BattlePlayer?>()

    private fun subscribeToPlayerEvents(player: BattlePlayer) {
        player.onConnected.subscribe(BattleManager) { onConnected.fire(it) }
        player.onGameStarted.subscribe(BattleManager) { onGameStarted.fire(it) }
        player.onGameEnded.subscribe(BattleManager) { onGameEnded.fire(it) }
        player.onGameAborted.subscribe(BattleManager) { onGameAborted.fire(it) }
        player.onGamePaused.subscribe(BattleManager) { onGamePaused.fire(it) }
        player.onGameResumed.subscribe(BattleManager) { onGameResumed.fire(it) }
        player.onRoundStarted.subscribe(BattleManager) { onRoundStarted.fire(it) }
        player.onRoundEnded.subscribe(BattleManager) { onRoundEnded.fire(it) }
        player.onTickEvent.subscribe(BattleManager) { onTickEvent.fire(it) }
        player.onBotListUpdate.subscribe(BattleManager) { onBotListUpdate.fire(it) }
        player.onStdOutputUpdated.subscribe(BattleManager) { onStdOutputUpdated.fire(it) }

        onPlayerChanged.fire(player)
    }

    private fun unsubscribeFromPlayerEvents(player: BattlePlayer) {
        player.onConnected.unsubscribe(BattleManager)
        player.onGameStarted.unsubscribe(BattleManager)
        player.onGameEnded.unsubscribe(BattleManager)
        player.onGameAborted.unsubscribe(BattleManager)
        player.onGamePaused.unsubscribe(BattleManager)
        player.onGameResumed.unsubscribe(BattleManager)
        player.onRoundStarted.unsubscribe(BattleManager)
        player.onRoundEnded.unsubscribe(BattleManager)
        player.onTickEvent.unsubscribe(BattleManager)
        player.onBotListUpdate.unsubscribe(BattleManager)
        player.onStdOutputUpdated.unsubscribe(BattleManager)

        onPlayerChanged.fire(null)
    }
}
