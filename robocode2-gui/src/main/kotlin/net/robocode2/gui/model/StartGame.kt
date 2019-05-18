package net.robocode2.gui.model

data class StartGame(
        override val clientKey: String,
        val gameSetup: GameSetup,
        val botAddresses: Set<BotAddress>
) : ClientMessage(MessageType.START_GAME.type, clientKey)