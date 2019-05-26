package net.robocode2.gui.model

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
sealed class ClientMessage : Message() {
    abstract val clientKey: String
}

@Serializable
@SerialName("BotDeathEvent")
data class BotDeathEvent(
        override val turnNumber: Int,
        val victimId: Int
) : Event()

@Serializable
@SerialName("BotHitWallEvent")
data class BotHitWallEvent(
        override val turnNumber: Int,
        val victimId: Int
) : Event()

@Serializable
@SerialName("BotHitBotEvent")
data class BotHitBotEvent(
        override val turnNumber: Int,
        val victimId: Int,
        val botId: Int,
        val energy: Double,
        val x: Double,
        val y: Double,
        val rammed: Boolean
) : Event()

@Serializable
@SerialName("BulletFiredEvent")
data class BulletFiredEvent(
        override val turnNumber: Int,
        val bullet: BulletState
) : Event()

@Serializable
@SerialName("BulletHitBotEvent")
data class BulletHitBotEvent(
        override val turnNumber: Int,
        val victimId: Int,
        val bullet: BulletState,
        val damage: Double,
        val energy: Double
) : Event()

@Serializable
@SerialName("BulletHitBulletEvent")
data class BulletHitBulletEvent(
        override val turnNumber: Int,
        val bullet: BulletState,
        val hitBullet: BulletState
) : Event()

@Serializable
@SerialName("BulletHitWallEvent")
data class BulletHitWallEvent(
        override val turnNumber: Int,
        val bullet: BulletState
) : Event()

@Serializable
@SerialName("HitByBulletEvent")
data class HitByBulletEvent(
        override val turnNumber: Int,
        val bullet: BulletState,
        val damage: Double,
        val energy: Double
) : Event()

@Serializable
@SerialName("ScannedBotEvent")
data class ScannedBotEvent(
        override val turnNumber: Int,
        val scannedByBotId: Int,
        val scannedBotId: Int,
        val energy: Double,
        val x: Double,
        val y: Double,
        val direction: Double,
        val speed: Double
) : Event()

@Serializable
@SerialName("TickEventForObserver")
open class TickEvent(
        override val turnNumber: Int,
        val roundNumber: Int,
        val botStates: Set<BotState>,
        val bulletStates: Set<BulletState>,
        val events: Set<Message>
) : Event()

@Serializable
@SerialName("BotListUpdate")
data class BotListUpdate(
        val bots: Set<BotInfo>
) : Message()

@Serializable
@SerialName("GameAbortedEvent")
sealed class GameAbortedEvent : Message()

@Serializable
@SerialName("GameEndedEvent")
data class GameEndedEvent(
        val numberOfRounds: Int,
        val results: List<BotResults>
) : Message()

@Serializable
@SerialName("GamePausedEvent")
sealed class GamePausedEvent : Message()

@Serializable
@SerialName("GameResumedEvent")
sealed class GameResumedEvent : Message()

@Serializable
@SerialName("GameStartedEventForObserver")
data class GameStartedEvent(
        val gameSetup: GameSetup,
        val participants: List<Participant>
) : Message()


@Serializable
@SerialName("ControllerHandshake")
data class ControllerHandshake(
        override val clientKey: String,
        val name: String,
        val version: String,
        val author: String?
) : ClientMessage()

@Serializable
@SerialName("ServerHandshake")
data class ServerHandshake(
        override val clientKey: String,
        val variant: String,
        val version: String,
        val games: Set<GameSetup>
) : ClientMessage()

@Serializable
@SerialName("StartGame")
data class StartGame(
        override val clientKey: String,
        val gameSetup: GameSetup,
        val botAddresses: Set<BotAddress>
) : ClientMessage()

@Serializable
@SerialName("StopGame")
class StopGame(
        override val clientKey: String
) : ClientMessage()

val messageModule = SerializersModule {
    polymorphic(Message::class) {
        BotDeathEvent::class with BotDeathEvent.serializer()
        BotHitWallEvent::class with BotHitWallEvent.serializer()
        BotHitBotEvent::class with BotHitBotEvent.serializer()
        BotListUpdate::class with BotListUpdate.serializer()
        BulletFiredEvent::class with BulletFiredEvent.serializer()
        BulletHitBotEvent::class with BulletHitBotEvent.serializer()
        BulletHitBulletEvent::class with BulletHitBulletEvent.serializer()
        BulletHitWallEvent::class with BulletHitWallEvent.serializer()
        ControllerHandshake::class with ControllerHandshake.serializer()
        GameAbortedEvent::class with GameAbortedEvent.serializer()
        GameEndedEvent::class with GameEndedEvent.serializer()
        GameResumedEvent::class with GameResumedEvent.serializer()
        GameStartedEvent::class with GameStartedEvent.serializer()
        HitByBulletEvent::class with HitByBulletEvent.serializer()
        ScannedBotEvent::class with ScannedBotEvent.serializer()
        ServerHandshake::class with ServerHandshake.serializer()
        StartGame::class with StartGame.serializer()
        StopGame::class with StopGame.serializer()
        TickEvent::class with TickEvent.serializer()
    }
}


fun main() {
    val json = Json(context = messageModule)


    val str = json.stringify(PolymorphicSerializer(Message::class), BotDeathEvent(1, 2))
    println(str)

    val message = json.parse(PolymorphicSerializer(Message::class), str)
    println(message)
}