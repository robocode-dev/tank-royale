package dev.robocode.tankroyale.gui.client

import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.gui.client.ClientEvents.onBotListUpdate
import dev.robocode.tankroyale.gui.client.ClientEvents.onConnected
import dev.robocode.tankroyale.gui.client.ClientEvents.onGameAborted
import dev.robocode.tankroyale.gui.client.ClientEvents.onGameEnded
import dev.robocode.tankroyale.gui.client.ClientEvents.onGamePaused
import dev.robocode.tankroyale.gui.client.ClientEvents.onGameResumed
import dev.robocode.tankroyale.gui.client.ClientEvents.onGameStarted
import dev.robocode.tankroyale.gui.client.ClientEvents.onPlayerChanged
import dev.robocode.tankroyale.gui.client.ClientEvents.onRoundEnded
import dev.robocode.tankroyale.gui.client.ClientEvents.onRoundStarted
import dev.robocode.tankroyale.gui.client.ClientEvents.onSeekToTurn
import dev.robocode.tankroyale.gui.client.ClientEvents.onStdOutputUpdated
import dev.robocode.tankroyale.gui.client.ClientEvents.onTickEvent
import dev.robocode.tankroyale.gui.player.BattlePlayer
import dev.robocode.tankroyale.gui.player.LiveBattlePlayer
import dev.robocode.tankroyale.gui.ui.tps.TpsEvents

/**
 * Manages the lifecycle and coordination of battle players.
 * Acts as a central coordinator between the UI and different battle player implementations.
 */
object Client {

    private var liveBattlePlayer: LiveBattlePlayer = LiveBattlePlayer()
    private var currentPlayer: BattlePlayer? = null

    init {
        // Subscribe to bot policy changes
        ClientEvents.onBotPolicyChanged.subscribe(Client) {
            currentPlayer?.changeBotPolicy(it)
        }
        TpsEvents.onTpsChanged.subscribe(Client) {
            currentPlayer?.changeTps(it.tps)
        }
    }

    val currentGameSetup: GameSetup?
        get() = currentPlayer?.getCurrentGameSetup()

    val currentTick: TickEvent?
        get() = currentPlayer?.getCurrentTick()

    fun switchToLiveBattlePlayer() {
        setPlayer(liveBattlePlayer)
    }

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
        player.start()

        // Fire event that player has changed
        onPlayerChanged.fire(player)
    }

    fun isLivePlayerConnected(): Boolean =
        currentPlayer == liveBattlePlayer && liveBattlePlayer.isCorrectlyConnected()

    fun close() {
        liveBattlePlayer.close()
    }

    fun start() {
        currentPlayer?.start() ?: throw IllegalStateException("No active battle player")
    }

    fun startGame(botAddresses: Set<BotAddress>) {
        if (currentPlayer == liveBattlePlayer) {
            liveBattlePlayer.startGame(botAddresses)
        } else {
            throw IllegalStateException("Trying to start game with websocket bots without server connection")
        }
    }

    fun stopGame() {
        currentPlayer?.stop()
    }

    fun restartGame() {
        currentPlayer?.restart()
    }

    fun pauseGame() {
        currentPlayer?.pause()
    }

    fun resumeGame() {
        currentPlayer?.resume()
    }

    internal fun doNextTurn() {
        currentPlayer?.nextTurn()
    }

    fun isGameRunning(): Boolean = currentPlayer?.isRunning() ?: false

    fun isGamePaused(): Boolean = currentPlayer?.isPaused() ?: false

    val joinedBots: Set<BotInfo>
        get() = currentPlayer?.getJoinedBots() ?: emptySet()

    fun getParticipant(botId: Int): Participant {
        return currentPlayer?.getParticipant(botId)
            ?: throw IllegalStateException("No battle player available")
    }

    fun getStandardOutput(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>? {
        return currentPlayer?.getStandardOutput(botId)
    }

    fun getStandardError(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>? {
        return currentPlayer?.getStandardError(botId)
    }

    private fun subscribeToPlayerEvents(player: BattlePlayer) {
        player.onConnected.subscribe(Client) { onConnected.fire(it) }
        player.onGameStarted.subscribe(Client) { onGameStarted.fire(it) }
        player.onGameEnded.subscribe(Client) { onGameEnded.fire(it) }
        player.onGameAborted.subscribe(Client) { onGameAborted.fire(it) }
        player.onGamePaused.subscribe(Client) { onGamePaused.fire(it) }
        player.onGameResumed.subscribe(Client) { onGameResumed.fire(it) }
        player.onRoundStarted.subscribe(Client) { onRoundStarted.fire(it) }
        player.onRoundEnded.subscribe(Client) { onRoundEnded.fire(it) }
        player.onTickEvent.subscribe(Client) { onTickEvent.fire(it) }
        player.onBotListUpdate.subscribe(Client) { onBotListUpdate.fire(it) }
        player.onStdOutputUpdated.subscribe(Client) { onStdOutputUpdated.fire(it) }
        player.onSeekToTurn.subscribe(Client) { onSeekToTurn.fire(it) }
    }

    private fun unsubscribeFromPlayerEvents(player: BattlePlayer) {
        player.onConnected.unsubscribe(Client)
        player.onGameStarted.unsubscribe(Client)
        player.onGameEnded.unsubscribe(Client)
        player.onGameAborted.unsubscribe(Client)
        player.onGamePaused.unsubscribe(Client)
        player.onGameResumed.unsubscribe(Client)
        player.onRoundStarted.unsubscribe(Client)
        player.onRoundEnded.unsubscribe(Client)
        player.onTickEvent.unsubscribe(Client)
        player.onBotListUpdate.unsubscribe(Client)
        player.onStdOutputUpdated.unsubscribe(Client)
        player.onSeekToTurn.unsubscribe(Client)
    }
}
