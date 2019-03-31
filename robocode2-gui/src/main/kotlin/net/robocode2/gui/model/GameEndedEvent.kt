package net.robocode2.gui.model

data class GameEndedEvent(val numberOfRounds: Int, val results: Set<BotResults>)
    : Content(type = ContentType.GAME_ENDED_EVENT.type)
