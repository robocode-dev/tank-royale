package net.robocode2.gui.model

import kotlin.reflect.KClass

class ContentAdapter: com.beust.klaxon.TypeAdapter<Content> {
    override fun classFor(type: Any): KClass<out Content> = when(type as String) {
        ContentType.SERVER_HANDSHAKE.type -> ServerHandshake::class
        ContentType.TICK_EVENT.type -> TickEvent::class
        else -> throw IllegalArgumentException("Unknown message type: $type")
    }
}