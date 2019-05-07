package net.robocode2.gui.model

import kotlin.reflect.KClass

class ContentAdapter: com.beust.klaxon.TypeAdapter<Content> {
    override fun classFor(type: Any): KClass<out Content> = when(type as String) {
        ContentType.SERVER_HANDSHAKE.type -> ServerHandshake::class
        ContentType.BOT_LIST_UPDATE.type -> BotListUpdate::class
        ContentType.TICK_EVENT.type -> TickEvent::class
        ContentType.GAME_STARTED_EVENT.type -> GameStartedEvent::class
        ContentType.GAME_ENDED_EVENT.type -> GameEndedEvent::class
        ContentType.GAME_ABORTED_EVENT.type -> GameAbortedEvent::class
        ContentType.GAME_PAUSED_EVENT.type -> GamePausedEvent::class
        ContentType.GAME_RESUMED_EVENT.type -> GameResumedEvent::class
        ContentType.SCANNED_BOT_EVENT.type -> ScannedBotEvent::class
        ContentType.BOT_DEATH_EVENT.type -> BotDeathEvent::class
        ContentType.BOT_HIT_BOT_EVENT.type -> BotHitWallEvent::class
        ContentType.BOT_HIT_WALL_EVENT.type -> BotHitWallEvent::class
        ContentType.BULLET_FIRED_EVENT.type -> BulletFiredEvent::class
        ContentType.BULLET_HIT_BOT_EVENT.type -> BulletHitBotEvent::class
        ContentType.BULLET_HIT_BULLET_EVENT.type -> BulletHitBulletEvent::class
        ContentType.BULLET_HIT_WALL_EVENT.type -> BulletHitWallEvent::class
        ContentType.HIT_BY_BULLET_EVENT.type -> HitByBulletEvent::class
        else -> throw IllegalStateException("Unknown message type: $type")
    }
}