package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.game.*

object EventsMapper {
    fun map(events: Set<dev.robocode.tankroyale.server.event.Event>): List<Event> {
        val mappedEvents = mutableListOf<Event>()
        events.forEach { mappedEvents += map(it) }
        return mappedEvents
    }

    private fun map(event: dev.robocode.tankroyale.server.event.Event): Event {
        return when (event) {
            is dev.robocode.tankroyale.server.event.BotDeathEvent -> map(event)
            is dev.robocode.tankroyale.server.event.BotHitBotEvent -> map(event)
            is dev.robocode.tankroyale.server.event.BotHitWallEvent -> map(event)
            is dev.robocode.tankroyale.server.event.BulletFiredEvent -> map(event)
            is dev.robocode.tankroyale.server.event.BulletHitBotEvent -> map(event)
            is dev.robocode.tankroyale.server.event.BulletHitBulletEvent -> map(event)
            is dev.robocode.tankroyale.server.event.BulletHitWallEvent -> map(event)
            is dev.robocode.tankroyale.server.event.ScannedBotEvent -> map(event)
            is dev.robocode.tankroyale.server.event.SkippedTurnEvent -> map(event)
            is dev.robocode.tankroyale.server.event.TeamMessageEvent -> map(event)
            is dev.robocode.tankroyale.server.event.WonRoundEvent -> map(event)
            else -> throw IllegalStateException("Event type not handled: ${event.javaClass.canonicalName}")
        }
    }

    private fun map(botDeathEvent: dev.robocode.tankroyale.server.event.BotDeathEvent): BotDeathEvent {
        val event = BotDeathEvent()
        event.type = Message.Type.BOT_DEATH_EVENT
        botDeathEvent.apply {
            event.turnNumber = turnNumber
            event.victimId = victimId.value
        }
        return event
    }

    private fun map(botHitBotEvent: dev.robocode.tankroyale.server.event.BotHitBotEvent): BotHitBotEvent {
        val event = BotHitBotEvent()
        event.type = Message.Type.BOT_HIT_BOT_EVENT
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
        event.type = Message.Type.BOT_HIT_WALL_EVENT
        botHitWallEvent.apply {
            event.turnNumber = turnNumber
            event.victimId = victimId.value
        }
        return event
    }

    private fun map(bulletFiredEvent: dev.robocode.tankroyale.server.event.BulletFiredEvent): BulletFiredEvent {
        val event = BulletFiredEvent()
        event.type = Message.Type.BULLET_FIRED_EVENT
        bulletFiredEvent.apply {
            event.turnNumber = turnNumber
            event.bullet = BulletToBulletStateMapper.map(bullet)
        }
        return event
    }

    private fun map(bulletHitBotEvent: dev.robocode.tankroyale.server.event.BulletHitBotEvent): BulletHitBotEvent {
        val event = BulletHitBotEvent()
        event.type = Message.Type.BULLET_HIT_BOT_EVENT
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
        event.type = Message.Type.BULLET_HIT_BULLET_EVENT
        bulletHitBulletEvent.apply {
            event.turnNumber = turnNumber
            event.bullet = BulletToBulletStateMapper.map(bullet)
            event.hitBullet = BulletToBulletStateMapper.map(hitBullet)
        }
        return event
    }

    private fun map(bulletHitWallEvent: dev.robocode.tankroyale.server.event.BulletHitWallEvent): BulletHitWallEvent {
        val event = BulletHitWallEvent()
        event.type = Message.Type.BULLET_HIT_WALL_EVENT
        bulletHitWallEvent.apply {
            event.turnNumber = turnNumber
            event.bullet = BulletToBulletStateMapper.map(bullet)
        }
        return event
    }

    private fun map(scannedBotEvent: dev.robocode.tankroyale.server.event.ScannedBotEvent): ScannedBotEvent {
        val event = ScannedBotEvent()
        event.type = Message.Type.SCANNED_BOT_EVENT
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
        event.type = Message.Type.SKIPPED_TURN_EVENT
        event.turnNumber = skippedTurnEvent.turnNumber
        return event
    }

    private fun map(wonRoundEvent: dev.robocode.tankroyale.server.event.WonRoundEvent): WonRoundEvent {
        val event = WonRoundEvent()
        event.type = Message.Type.WON_ROUND_EVENT
        event.turnNumber = wonRoundEvent.turnNumber
        return event
    }

    private fun map(teamMessageEvent: dev.robocode.tankroyale.server.event.TeamMessageEvent): TeamMessageEvent {
        val event = TeamMessageEvent()
        event.type = Message.Type.TEAM_MESSAGE_EVENT

        teamMessageEvent.apply {
            event.turnNumber = turnNumber
            event.message = message
            event.messageType = messageType
            event.senderId = senderId.value
        }
        return event
    }
}