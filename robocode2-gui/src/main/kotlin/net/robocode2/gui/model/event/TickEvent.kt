package net.robocode2.gui.model.event

import net.robocode2.gui.model.comm.Content
import net.robocode2.gui.model.comm.ContentType

class TickEvent(
        val roundState: RoundState,
        val botStates: Set<BotState>,
        val bulletStates: Set<BulletState>,
        val events: Set<Content>
) : Content(type = ContentType.TICK_EVENT.type)
