package net.robocode2.gui.model

class GameStartedEvent(
        val gameSetup: GameSetup,
        val participants: Set<Participant>
) : Content(type = ContentType.GAME_STARTED_EVENT.type)
