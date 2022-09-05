package dev.robocode.tankroyale.gui.model

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

sealed class MessageConstants {
    companion object {
        val json = Json {
            classDiscriminator = "type"
            serializersModule = messageModule
        }
    }
}

@Polymorphic
@Serializable
sealed class Message

@Serializable
sealed class Event : Message() {
    abstract val turnNumber: Int
}

@Serializable
@SerialName("BotDeathEvent")
class BotDeathEvent(
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
@SerialName("GameStartedEventForObserver")
data class GameStartedEvent(
    val gameSetup: GameSetup,
    val participants: List<Participant>
) : Message()

@Serializable
@SerialName("GameAbortedEvent")
class GameAbortedEvent : Message()

@Serializable
@SerialName("GameEndedEventForObserver")
data class GameEndedEvent(
    val numberOfRounds: Int,
    val results: List<BotResults>
) : Message()

@Serializable
@SerialName("GamePausedEventForObserver")
class GamePausedEvent : Message()

@Serializable
@SerialName("GameResumedEventForObserver")
class GameResumedEvent : Message()

@Serializable
@SerialName("RoundStartedEvent")
class RoundStartedEvent(
    val roundNumber: Int // must be present for serialization
) : Message()

@Serializable
@SerialName("RoundEndedEvent")
class RoundEndedEvent(
    val roundNumber: Int, // must be present for serialization
    val turnNumber: Int // must be present for serialization
) : Message()

@Serializable
@SerialName("TpsChangedEvent")
data class TpsChangedEvent(
    val tps: Int
) : Message()

@Serializable
@SerialName("ControllerHandshake")
data class ControllerHandshake(
    val sessionId: String,
    val name: String,
    val version: String,
    val author: String?,
    val secret: String?
) : Message()

@Serializable
@SerialName("ServerHandshake")
data class ServerHandshake(
    val sessionId: String,
    val variant: String,
    val version: String,
    val gameTypes: Set<String>
) : Message()

@Serializable
@SerialName("StartGame")
data class StartGame(
    val gameSetup: GameSetup,
    val botAddresses: Set<BotAddress>
) : Message()

@Serializable
@SerialName("StopGame")
class StopGame : Message()

@Serializable
@SerialName("PauseGame")
class PauseGame : Message()

@Serializable
@SerialName("ResumeGame")
class ResumeGame : Message()

@Serializable
@SerialName("NextTurn")
class NextTurn : Message()

@Serializable
@SerialName("ChangeTps")
data class ChangeTps(
    val tps: Int
) : Message()

val messageModule = SerializersModule {
    polymorphic(Message::class) {
        subclass(BotDeathEvent::class)
        subclass(BotDeathEvent::class)
        subclass(BotHitWallEvent::class)
        subclass(BotHitBotEvent::class)
        subclass(BotListUpdate::class)
        subclass(BulletFiredEvent::class)
        subclass(BulletHitBotEvent::class)
        subclass(BulletHitBulletEvent::class)
        subclass(BulletHitWallEvent::class)
        subclass(ChangeTps::class)
        subclass(ControllerHandshake::class)
        subclass(GameAbortedEvent::class)
        subclass(GameEndedEvent::class)
        subclass(GamePausedEvent::class)
        subclass(GameResumedEvent::class)
        subclass(GameStartedEvent::class)
        subclass(HitByBulletEvent::class)
        subclass(PauseGame::class)
        subclass(ResumeGame::class)
        subclass(NextTurn::class)
        subclass(RoundEndedEvent::class)
        subclass(RoundStartedEvent::class)
        subclass(GamePausedEvent::class)
        subclass(ScannedBotEvent::class)
        subclass(ServerHandshake::class)
        subclass(StartGame::class)
        subclass(StopGame::class)
        subclass(TickEvent::class)
        subclass(TpsChangedEvent::class)
    }
}

fun main() {
    val json = MessageConstants.json

    val str = json.encodeToString(PolymorphicSerializer(Message::class), BotDeathEvent(1, 2))
    println(str)

    val message = json.decodeFromString(PolymorphicSerializer(Message::class), str)
    println(message)
}