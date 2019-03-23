package net.robocode2.gui.model

class StartGame(
        val gameSetup: GameSetup,
        val botAddresses: Set<BotAddress>
) : Content(type = ContentType.START_GAME.type)