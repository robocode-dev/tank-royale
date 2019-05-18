package net.robocode2.gui.model

data class GameEndedEvent(
        val numberOfRounds: Int,
        val results: List<BotResults>
) : Message(MessageType.GAME_ENDED_EVENT.type)
