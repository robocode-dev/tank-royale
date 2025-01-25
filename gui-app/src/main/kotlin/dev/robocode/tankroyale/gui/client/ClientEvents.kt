package dev.robocode.tankroyale.gui.client

import dev.robocode.tankroyale.gui.model.*
import dev.robocode.tankroyale.gui.util.Event

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
}