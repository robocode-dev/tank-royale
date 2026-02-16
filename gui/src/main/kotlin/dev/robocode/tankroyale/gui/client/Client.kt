package dev.robocode.tankroyale.gui.client

import dev.robocode.tankroyale.common.event.On
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
        ClientEvents.onBotPolicyChanged+= On(Client) {
            currentPlayer?.changeBotPolicy(it)
        }
        TpsEvents.onTpsChanged+= On(Client) {
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
        onPlayerChanged(player)
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
        player.onConnected+= On(Client) { onConnected(it) }
        player.onGameStarted+= On(Client) { onGameStarted(it) }
        player.onGameEnded+= On(Client) { onGameEnded(it) }
        player.onGameAborted+= On(Client) { onGameAborted(it) }
        player.onGamePaused+= On(Client) { onGamePaused(it) }
        player.onGameResumed+= On(Client) { onGameResumed(it) }
        player.onRoundStarted+= On(Client) { onRoundStarted(it) }
        player.onRoundEnded+= On(Client) { onRoundEnded(it) }
        player.onTickEvent+= On(Client) { onTickEvent(it) }
        player.onBotListUpdate+= On(Client) { onBotListUpdate(it) }
        player.onStdOutputUpdated+= On(Client) { onStdOutputUpdated(it) }
        player.onSeekToTurn+= On(Client) { onSeekToTurn(it) }
    }

    private fun unsubscribeFromPlayerEvents(player: BattlePlayer) {
        player.onConnected -= Client
        player.onGameStarted -= Client
        player.onGameEnded -= Client
        player.onGameAborted -= Client
        player.onGamePaused -= Client
        player.onGameResumed -= Client
        player.onRoundStarted -= Client
        player.onRoundEnded -= Client
        player.onTickEvent -= Client
        player.onBotListUpdate -= Client
        player.onStdOutputUpdated -= Client
        player.onSeekToTurn -= Client
    }
}
