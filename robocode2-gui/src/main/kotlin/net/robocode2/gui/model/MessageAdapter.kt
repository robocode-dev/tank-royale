package net.robocode2.gui.model

import kotlin.reflect.KClass

class MessageAdapter: com.beust.klaxon.TypeAdapter<Message> {
    override fun classFor(type: Any): KClass<out Message> = when(type as String) {
        MessageType.SERVER_HANDSHAKE.type -> ServerHandshake::class
        MessageType.GAME_SETUP.type -> GameSetup::class
        else -> throw IllegalArgumentException("Unknown type: $type")
    }
}