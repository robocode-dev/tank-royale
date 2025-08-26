package dev.robocode.tankroyale.gui.client

import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.common.Event
import dev.robocode.tankroyale.gui.player.BattlePlayer

object ClientEvents {
    val onConnected = Event<Unit>()
    val onBotListUpdate = Event<BotListUpdate>()
    val onGameStarted = Event<GameStartedEvent>()
    val onGameEnded = Event<GameEndedEvent>()
    val onGameAborted = Event<GameAbortedEvent>()
    val onGamePaused = Event<GamePausedEvent>()
    val onGameResumed = Event<GameResumedEvent>()
    val onRoundStarted = Event<RoundStartedEvent>()
    val onRoundEnded = Event<RoundEndedEvent>()
    val onTickEvent = Event<TickEvent>()

    val onStdOutputUpdated = Event<TickEvent>()
    val onBotPolicyChanged = Event<BotPolicyUpdate>()

    /** Fired when the active battle player changes */
    val onPlayerChanged = Event<BattlePlayer>()

    /** Fired when the user seeks to a specific position when replaying a battle */
    val onSeekToTurn = Event<TickEvent>()
}