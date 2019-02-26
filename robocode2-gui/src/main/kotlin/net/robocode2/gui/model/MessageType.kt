package net.robocode2.gui.model

enum class MessageType(val type: String) {
    SERVER_HANDSHAKE("serverHandshake"),
    GAME_SETUP("gameSetup")
}