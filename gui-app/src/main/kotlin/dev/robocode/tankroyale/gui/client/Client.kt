package dev.robocode.tankroyale.gui.client

import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.gui.player.BattleManager
import dev.robocode.tankroyale.gui.player.LiveBattlePlayer

/**
 * Client facade for backward compatibility.
 * Delegates to BattleManager and LiveBattlePlayer for actual functionality.
 */
object Client {

    private var liveBattlePlayer: LiveBattlePlayer? = null

    init {
        // Forward BattleManager events to ClientEvents for backward compatibility
        BattleManager.onConnected.subscribe(Client) { ClientEvents.onConnected.fire(it) }
        BattleManager.onBotListUpdate.subscribe(Client) { ClientEvents.onBotListUpdate.fire(it) }
        BattleManager.onGameStarted.subscribe(Client) { ClientEvents.onGameStarted.fire(it) }
        BattleManager.onGameEnded.subscribe(Client) { ClientEvents.onGameEnded.fire(it) }
        BattleManager.onGameAborted.subscribe(Client) { ClientEvents.onGameAborted.fire(it) }
        BattleManager.onGamePaused.subscribe(Client) { ClientEvents.onGamePaused.fire(it) }
        BattleManager.onGameResumed.subscribe(Client) { ClientEvents.onGameResumed.fire(it) }
        BattleManager.onRoundStarted.subscribe(Client) { ClientEvents.onRoundStarted.fire(it) }
        BattleManager.onRoundEnded.subscribe(Client) { ClientEvents.onRoundEnded.fire(it) }
        BattleManager.onTickEvent.subscribe(Client) { ClientEvents.onTickEvent.fire(it) }
        BattleManager.onStdOutputUpdated.subscribe(Client) { ClientEvents.onStdOutputUpdated.fire(it) }

        // Subscribe to bot policy changes
        ClientEvents.onBotPolicyChanged.subscribe(Client) {
            liveBattlePlayer?.changeBotPolicy(it)
        }
    }

    val currentGameSetup: GameSetup?
        get() = BattleManager.getCurrentGameSetup()

    val currentTick: TickEvent?
        get() = BattleManager.getCurrentTick()

    fun isConnected(): Boolean = liveBattlePlayer?.isConnected() ?: false

    fun connect() {
        if (liveBattlePlayer == null) {
            liveBattlePlayer = LiveBattlePlayer()
            BattleManager.setPlayer(liveBattlePlayer!!)
        }
        BattleManager.start()
    }

    fun close() {
        BattleManager.stop()
        BattleManager.clearPlayer()
        liveBattlePlayer = null
    }

    fun startGame(botAddresses: Set<BotAddress>) {
        ensureLivePlayer()
        liveBattlePlayer!!.startGame(botAddresses)
    }

    fun stopGame() {
        BattleManager.stop()
    }

    fun restartGame() {
        BattleManager.restart()
    }

    fun pauseGame() {
        BattleManager.pause()
    }

    fun resumeGame() {
        BattleManager.resume()
    }

    internal fun doNextTurn() {
        BattleManager.nextTurn()
    }

    fun isGameRunning(): Boolean = BattleManager.isRunning()

    fun isGamePaused(): Boolean = BattleManager.isPaused()

    val joinedBots: Set<BotInfo>
        get() = liveBattlePlayer?.joinedBots ?: emptySet()

    fun getParticipant(botId: Int): Participant {
        return liveBattlePlayer?.getParticipant(botId)
            ?: throw IllegalStateException("No live battle player available")
    }

    fun getStandardOutput(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>? {
        return liveBattlePlayer?.getStandardOutput(botId)
    }

    fun getStandardError(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>? {
        return liveBattlePlayer?.getStandardError(botId)
    }

    private fun ensureLivePlayer() {
        if (liveBattlePlayer == null) {
            liveBattlePlayer = LiveBattlePlayer()
            BattleManager.setPlayer(liveBattlePlayer!!)
        }
    }
}
