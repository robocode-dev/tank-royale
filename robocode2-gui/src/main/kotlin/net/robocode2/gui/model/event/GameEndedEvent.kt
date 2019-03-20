package net.robocode2.gui.model.event

import net.robocode2.gui.model.comm.Content
import net.robocode2.gui.model.comm.ContentType

class GameEndedEvent(
        val numberOfRounds: Int,
        val results: Set<BotResults>
) : Content(type = ContentType.BOT_RESULTS.type)
