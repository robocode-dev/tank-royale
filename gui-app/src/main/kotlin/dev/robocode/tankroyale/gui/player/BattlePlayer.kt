package dev.robocode.tankroyale.gui.player

import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.common.Event

/**
 * Abstract interface for controlling battles in the GUI.
 * Implementations include live battles (server-based) and replay battles (file-based).
 */
interface BattlePlayer {

    /**
     * Starts the battle. For live players, this connects to server and starts game.
     * For replay players, this begins playback from the beginning.
     */
    fun start()

    /**
     * Stops the current battle.
     */
    fun stop()

    /**
     * Pauses the current battle.
     */
    fun pause()

    /**
     * Resumes the current battle.
     */
    fun resume()

    /**
     * Advances to the next turn.
     */
    fun nextTurn()

    /**
     * Restarts the current battle.
     */
    fun restart()

    /**
     * Returns true if the battle is currently running.
     */
    fun isRunning(): Boolean

    /**
     * Returns true if the battle is currently paused.
     */
    fun isPaused(): Boolean

    /**
     * Gets the current game setup, if available.
     */
    fun getCurrentGameSetup(): GameSetup?

    /**
     * Gets the current tick event, if available.
     */
    fun getCurrentTick(): TickEvent?

    /**
     * Gets a participant by bot ID.
     */
    fun getParticipant(botId: Int): Participant

    /**
     * Gets the joined bots for this battle.
     */
    fun getJoinedBots(): Set<BotInfo>

    /**
     * Gets standard output for a specific bot.
     */
    fun getStandardOutput(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>?

    /**
     * Gets standard error for a specific bot.
     */
    fun getStandardError(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>?

    /**
     * Changes the bot policy for this battle player.
     */
    fun changeBotPolicy(botPolicyUpdate: BotPolicyUpdate)

    /**
     * Changes the TPS (Turns Per Second) for this battle player.
     */
    fun changeTps(tps: Int)

    // Events that can be fired by battle players

    /** Fired when connected to battle source */
    val onConnected: Event<Unit>

    /** Fired when battle starts */
    val onGameStarted: Event<GameStartedEvent>

    /** Fired when battle ends */
    val onGameEnded: Event<GameEndedEvent>

    /** Fired when battle is aborted */
    val onGameAborted: Event<GameAbortedEvent>

    /** Fired when battle is paused */
    val onGamePaused: Event<GamePausedEvent>

    /** Fired when battle is resumed */
    val onGameResumed: Event<GameResumedEvent>

    /** Fired when round starts */
    val onRoundStarted: Event<RoundStartedEvent>

    /** Fired when round ends */
    val onRoundEnded: Event<RoundEndedEvent>

    /** Fired on each tick/frame */
    val onTickEvent: Event<TickEvent>

    /** Fired when bot list is updated */
    val onBotListUpdate: Event<BotListUpdate>

    /** Fired when standard output is updated */
    val onStdOutputUpdated: Event<TickEvent>

    /** Fired when the user seeks to a specific position when replaying a battle */
    val onSeekToTurn: Event<TickEvent>
}
