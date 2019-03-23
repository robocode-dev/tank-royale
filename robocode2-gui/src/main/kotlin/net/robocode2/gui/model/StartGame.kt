package net.robocode2.gui.model

import net.robocode2.gui.model.ClientContent
import net.robocode2.gui.model.ContentType
import net.robocode2.gui.model.GameSetup
import net.robocode2.gui.model.BotAddress

class StartGame(
        clientKey: String,
        val gameSetup: GameSetup,
        val botAddresses: Set<BotAddress>
) : ClientContent(type = ContentType.START_GAME.type, clientKey = clientKey)