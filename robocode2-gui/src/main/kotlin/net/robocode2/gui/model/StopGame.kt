package net.robocode2.gui.model

class StopGame(override val clientKey: String) : ClientMessage(MessageType.STOP_GAME.type, clientKey)