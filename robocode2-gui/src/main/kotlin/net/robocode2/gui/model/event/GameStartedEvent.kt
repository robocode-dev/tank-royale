package net.robocode2.gui.model.event

import net.robocode2.gui.model.GameSetup
import net.robocode2.gui.model.comm.Content
import net.robocode2.gui.model.comm.ContentType

class GameStartedEvent(
        val gameSetup: GameSetup,
        val participants: Set<Participant>
) : Content(type = ContentType.GAME_STARTED_EVENT.type)
