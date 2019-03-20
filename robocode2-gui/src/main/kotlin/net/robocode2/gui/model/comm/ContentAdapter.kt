package net.robocode2.gui.model.comm

import net.robocode2.gui.model.event.GameEndedEvent
import net.robocode2.gui.model.event.GameStartedEvent
import net.robocode2.gui.model.event.TickEvent
import kotlin.reflect.KClass

class ContentAdapter: com.beust.klaxon.TypeAdapter<Content> {
    override fun classFor(type: Any): KClass<out Content> = when(type as String) {
        ContentType.SERVER_HANDSHAKE.type -> ServerHandshake::class
        ContentType.BOT_LIST_UPDATE.type -> BotListUpdate::class
        ContentType.TICK_EVENT.type -> TickEvent::class
        ContentType.GAME_STARTED_EVENT.type -> GameStartedEvent::class
        ContentType.GAME_ENDED_EVENT.type -> GameEndedEvent::class
        else -> throw IllegalArgumentException("Unknown message type: $type")
    }
}