package net.robocode2.gui.model

import kotlin.reflect.KClass

class MessageAdapter: com.beust.klaxon.TypeAdapter<Message> {
    override fun classFor(type: Any): KClass<out Message> = when(type as String) {
        MessageType.SERVER_HANDSHAKE.type -> ServerHandshake::class
        MessageType.BOT_LIST_UPDATE.type -> BotListUpdate::class
        MessageType.TICK_EVENT.type -> TickEvent::class
        MessageType.GAME_STARTED_EVENT.type -> GameStartedEvent::class
        MessageType.GAME_ENDED_EVENT.type -> GameEndedEvent::class
        MessageType.GAME_ABORTED_EVENT.type -> GameAbortedEvent::class
        MessageType.GAME_PAUSED_EVENT.type -> GamePausedEvent::class
        MessageType.GAME_RESUMED_EVENT.type -> GameResumedEvent::class
        MessageType.SCANNED_BOT_EVENT.type -> ScannedBotEvent::class
        MessageType.BOT_DEATH_EVENT.type -> BotDeathEvent::class
        MessageType.BOT_HIT_BOT_EVENT.type -> BotHitWallEvent::class
        MessageType.BOT_HIT_WALL_EVENT.type -> BotHitWallEvent::class
        MessageType.BULLET_FIRED_EVENT.type -> BulletFiredEvent::class
        MessageType.BULLET_HIT_BOT_EVENT.type -> BulletHitBotEvent::class
        MessageType.BULLET_HIT_BULLET_EVENT.type -> BulletHitBulletEvent::class
        MessageType.BULLET_HIT_WALL_EVENT.type -> BulletHitWallEvent::class
        MessageType.HIT_BY_BULLET_EVENT.type -> HitByBulletEvent::class
        else -> throw IllegalStateException("Unknown message type: $type")
    }
}