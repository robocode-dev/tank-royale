package net.robocode2.gui.model

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@Polymorphic
@Serializable
sealed class Message

@Serializable
sealed class Event : Message() {
    abstract val turnNumber: Int
}

@Serializable
@SerialName("botDeathEvent")
data class BotDeathEvent(
        override val turnNumber: Int,
        val victimId: Int
) : Event()

@Serializable

@SerialName("botHitWallEvent")
data class BotHitWallEvent(
        override val turnNumber: Int,
        val victimId: Int
) : Event()

val messageModule = SerializersModule {
    polymorphic(Message::class) {
        BotDeathEvent::class with BotDeathEvent.serializer()
        BotHitWallEvent::class with BotHitWallEvent.serializer()
    }
}

fun main() {
    val json = Json(context = messageModule)


    val str = json.stringify(PolymorphicSerializer(Message::class), BotDeathEvent(1, 2))
    println(str)

    val message = json.parse(PolymorphicSerializer(Message::class), str)
    println(message)
}