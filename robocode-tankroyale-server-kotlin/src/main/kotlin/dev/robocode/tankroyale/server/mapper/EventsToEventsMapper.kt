package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.*
import java.util.ArrayList

object EventsToEventsMapper {
    fun map(events: Set<dev.robocode.tankroyale.server.event.Event>): List<Event> {
        val mappedEvents: MutableList<Event> = ArrayList()
        for (event in events) {
            mappedEvents.add(map(event))
        }
        return mappedEvents
    }

    private fun map(event: dev.robocode.tankroyale.server.event.Event): Event {
        if (event is dev.robocode.tankroyale.server.event.BotDeathEvent) return map(event)
        if (event is dev.robocode.tankroyale.server.event.BotHitBotEvent) return map(event)
        if (event is dev.robocode.tankroyale.server.event.BotHitWallEvent) return map(event)
        if (event is dev.robocode.tankroyale.server.event.BulletFiredEvent) return map(event)
        if (event is dev.robocode.tankroyale.server.event.BulletHitBotEvent) return map(event)
        if (event is dev.robocode.tankroyale.server.event.BulletHitBulletEvent) return map(event)
        if (event is dev.robocode.tankroyale.server.event.BulletHitWallEvent) return map(event)
        if (event is dev.robocode.tankroyale.server.event.ScannedBotEvent) return map(event)
        if (event is dev.robocode.tankroyale.server.event.SkippedTurnEvent) return map(event)
        throw IllegalStateException("Event type not handled: ${event.javaClass.canonicalName}")
    }

    private fun map(botDeathEvent: dev.robocode.tankroyale.server.event.BotDeathEvent): BotDeathEvent {
        val event = BotDeathEvent()
        event.`$type` = Message.`$type`.BOT_DEATH_EVENT
        botDeathEvent.apply {
            event.turnNumber = turnNumber
            event.victimId = victimId.value
        }
        return event
    }

    private fun map(botHitBotEvent: dev.robocode.tankroyale.server.event.BotHitBotEvent): BotHitBotEvent {
        val event = BotHitBotEvent()
        event.`$type` = Message.`$type`.BOT_HIT_BOT_EVENT
        botHitBotEvent.apply {
            event.turnNumber = turnNumber
            event.botId = botId.value
            event.victimId = victimId.value
            event.energy = energy
            event.x = x
            event.y = y
            event.rammed = isRammed
        }
        return event
    }

    private fun map(botHitWallEvent: dev.robocode.tankroyale.server.event.BotHitWallEvent): BotHitWallEvent {
        val event = BotHitWallEvent()
        event.`$type` = Message.`$type`.BOT_HIT_WALL_EVENT
        botHitWallEvent.apply {
            event.turnNumber = turnNumber
            event.victimId = victimId.value
        }
        return event
    }

    private fun map(bulletFiredEvent: dev.robocode.tankroyale.server.event.BulletFiredEvent): BulletFiredEvent {
        val event = BulletFiredEvent()
        event.`$type` = Message.`$type`.BULLET_FIRED_EVENT
        bulletFiredEvent.apply {
            event.turnNumber = turnNumber
            event.bullet = BulletToBulletStateMapper.map(bullet)
        }
        return event
    }

    private fun map(bulletHitBotEvent: dev.robocode.tankroyale.server.event.BulletHitBotEvent): BulletHitBotEvent {
        val event = BulletHitBotEvent()
        event.`$type` = Message.`$type`.BULLET_HIT_BOT_EVENT
        bulletHitBotEvent.apply {
            event.turnNumber = turnNumber
            event.bullet = BulletToBulletStateMapper.map(bullet)
            event.victimId = victimId.value
            event.damage = damage
            event.energy = energy
        }
        return event
    }

    private fun map(bulletHitBulletEvent: dev.robocode.tankroyale.server.event.BulletHitBulletEvent): BulletHitBulletEvent {
        val event = BulletHitBulletEvent()
        event.`$type` = Message.`$type`.BULLET_HIT_BULLET_EVENT
        bulletHitBulletEvent.apply {
            event.turnNumber = turnNumber
            event.bullet = BulletToBulletStateMapper.map(bullet)
            event.hitBullet = BulletToBulletStateMapper.map(hitBullet)
        }
        return event
    }

    private fun map(bulletHitWallEvent: dev.robocode.tankroyale.server.event.BulletHitWallEvent): BulletHitWallEvent {
        val event = BulletHitWallEvent()
        event.`$type` = Message.`$type`.BULLET_HIT_WALL_EVENT
        bulletHitWallEvent.apply {
            event.turnNumber = turnNumber
            event.bullet = BulletToBulletStateMapper.map(bullet)
        }
        return event
    }

    private fun map(scannedBotEvent: dev.robocode.tankroyale.server.event.ScannedBotEvent): ScannedBotEvent {
        val event = ScannedBotEvent()
        event.`$type` = Message.`$type`.SCANNED_BOT_EVENT
        scannedBotEvent.apply {
            event.turnNumber = turnNumber
            event.scannedByBotId = scannedByBotId.value
            event.scannedBotId = scannedBotId.value
            event.energy = energy
            event.x = x
            event.y = y
            event.direction = direction
            event.speed = speed
        }
        return event
    }

    private fun map(skippedTurnEvent: dev.robocode.tankroyale.server.event.SkippedTurnEvent): SkippedTurnEvent {
        val event = SkippedTurnEvent()
        event.`$type` = Message.`$type`.SKIPPED_TURN_EVENT
        event.turnNumber = skippedTurnEvent.turnNumber
        return event
    }
}