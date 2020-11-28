package dev.robocode.tankroyale.gui.model

enum class MessageType(val type: String) {
    // Messages
    SERVER_HANDSHAKE("ServerHandshake"),
    CONTROLLER_HANDSHAKE("ControllerHandshake"),
    BOT_LIST_UPDATE("BotListUpdate"),

    // Game Events
    TICK_EVENT("TickEventForObserver"),
    BOT_DEATH_EVENT("BotDeathEvent"),
    BOT_HIT_BOT_EVENT("BotHitBotEvent"),
    BOT_HIT_WALL_EVENT("BotHitWallEvent"),
    BULLET_FIRED_EVENT("BulletFiredEvent"),
    BULLET_HIT_BOT_EVENT("BulletHitBotEvent"),
    BULLET_HIT_BULLET_EVENT("BulletHitBulletEvent"),
    BULLET_HIT_WALL_EVENT("BulletHitWallEvent"),
    HIT_BY_BULLET_EVENT("HitByBulletEvent"),
    SCANNED_BOT_EVENT("ScannedBotEvent"),
    SKIPPED_TURN_EVENT("SkippedTurnEvent"),
    WON_ROUND_EVENT("WonRoundEvent"),

    // Controller Commands
    START_GAME("StartGame"),
    STOP_GAME("StopGame"),
    PAUSE_GAME("PauseGame"),
    RESUME_GAME("ResumeGame"),

    // Observer/controller Events
    GAME_STARTED_EVENT("GameStartedEventForObserver"),
    GAME_ENDED_EVENT("GameEndedEventForObserver"),
    GAME_ABORTED_EVENT("GameAbortedEventForObserver"),
    GAME_PAUSED_EVENT("GamePausedEventForObserver"),
    GAME_RESUMED_EVENT("GameResumedEventForObserver"),

    PARTICIPANT("Participant"),
    BOT_RESULTS("BotResultsForBot"),
}