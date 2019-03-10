package net.robocode2.gui.model

enum class ContentType(val type: String) {
    // Messages
    SERVER_HANDSHAKE("serverHandshake"),
    CONTROLLER_HANDSHAKE("controllerHandshake"),
    // Game Events
    BOT_DEATH_EVENT("botDeathEvent"),
    BOT_HIT_BOT_EVENT("botHitBotEvent"),
    BOT_HIT_WALL_EVENT("botHitWallEvent"),
    BULLET_FIRED_EVENT("bulletFiredEvent"),
    BULLET_MISSED_EVENT("bulletMissedEvent"),
    BULLET_HIT_BULLET_EVENT("bulletHitBulletEvent"),
    HIT_BY_BULLET("hitByBulletEvent"),
    SCANNED_BOT_EVENT("scannedBotEvent"),
    SKIPPED_TURN_EVENT("skippedTurnEvent"),
    TICK_EVENT("tickEventForObserver"),
    WON_ROUND_EVENT("wonRoundEvent"),
    // Controller Commands
    START_GAME("startGame"),
    STOP_GAME("stopGame"),
    PAUSE_GAME("pauseGame"),
    RESUME_GAME("resumeGame"),
    // Observer/controller Events
    GAME_ABORTED_EVENT("gameAbortedEventForObserver"),
    GAME_ENDED_EVENT("gameEndedEventForObserver"),
    GAME_PAUSED_EVENT("gamePausedEventForObserver"),
    GAME_RESUMED_EVENT("gameResumedEventForObserver"),
    GAME_STARTED_EVENT("gameStartedEventForObserver"),
}