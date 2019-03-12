package net.robocode2.gui.model.control

import net.robocode2.gui.model.Content
import net.robocode2.gui.model.ContentType
import net.robocode2.gui.model.GameSetup
import net.robocode2.gui.model.comm.BotAddress

data class StartGame(
        val gameSetup: GameSetup,
        val botAddresses: Set<BotAddress>
) : Content(type = ContentType.START_GAME.type)