package net.robocode2.gui.model

enum class MessageType(val type: String) {
    // Messages
    SERVER_HANDSHAKE("serverHandshake"),
    CONTROLLER_HANDSHAKE("controllerHandshake"),
    BOT_LIST_UPDATE("botListUpdate"),
    // Game Events
    TICK_EVENT("tickEventForObserver"),
    BOT_DEATH_EVENT("botDeathEvent"),
    BOT_HIT_BOT_EVENT("botHitBotEvent"),
    BOT_HIT_WALL_EVENT("botHitWallEvent"),
    BULLET_FIRED_EVENT("bulletFiredEvent"),
    BULLET_HIT_BOT_EVENT("bulletHitBotEvent"),
    BULLET_HIT_BULLET_EVENT("bulletHitBulletEvent"),
    BULLET_HIT_WALL_EVENT("bulletHitWallEvent"),
    HIT_BY_BULLET_EVENT("hitByBulletEvent"),
    SCANNED_BOT_EVENT("scannedBotEvent"),
    SKIPPED_TURN_EVENT("skippedTurnEvent"),
    WON_ROUND_EVENT("wonRoundEvent"),
    // Controller Commands
    START_GAME("startGame"),
    STOP_GAME("stopGame"),
    PAUSE_GAME("pauseGame"),
    RESUME_GAME("resumeGame"),
    // Observer/controller Events
    GAME_STARTED_EVENT("gameStartedEventForObserver"),
    GAME_ENDED_EVENT("gameEndedEventForObserver"),
    GAME_ABORTED_EVENT("gameAbortedEventForObserver"),
    GAME_PAUSED_EVENT("gamePausedEventForObserver"),
    GAME_RESUMED_EVENT("gameResumedEventForObserver"),

    PARTICIPANT("participant"),
    BOT_RESULTS("botResultsForBot"),
}