package net.robocode2.gui.model

data class GameStartedEvent(
        val gameSetup: GameSetup,
        val participant: Set<Participant>
) : Message(MessageType.GAME_STARTED_EVENT.type)
