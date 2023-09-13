package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.*

class BotEventsPanel(val bot: Participant) : ConsolePanel() {

    init {
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        ClientEvents.apply {
            onTickEvent.subscribe(this@BotEventsPanel) {
                dump(it.events)
            }
            onGameStarted.subscribe(this@BotEventsPanel) { gameStartedEvent ->
                if (gameStartedEvent.participants.any { it.displayName == bot.displayName }) {
                    subscribeToEvents()
                }
            }
            onGameEnded.subscribe(this@BotEventsPanel) {
                unsubscribeEvents()
            }
            onGameAborted.subscribe(this@BotEventsPanel) {
                unsubscribeEvents()
            }
        }
    }

    private fun unsubscribeEvents() {
        ClientEvents.apply {
            onRoundStarted.unsubscribe(this@BotEventsPanel)
            onTickEvent.unsubscribe(this@BotEventsPanel)
            onGameAborted.unsubscribe(this@BotEventsPanel)
            onGameEnded.unsubscribe(this@BotEventsPanel)
        }
    }

    private fun dump(events: Set<Event>) {
        events.forEach { event ->
            when (event) {
                is BotDeathEvent -> dumpBotDeathEvent(event)
                is BotHitWallEvent -> dumpBotHitWallEvent(event)
                is BotHitBotEvent -> dumpBotHitBotEvent(event)
                is BulletFiredEvent -> dumpBulletFiredEvent(event)
                is BulletHitBotEvent -> dumpBulletHitBotEvent(event)
                is BulletHitBulletEvent -> dumpBulletHitBulletEvent(event)
                is BulletHitWallEvent -> dumpBulletHitWallEvent(event)
                is ScannedBotEvent -> dumpScannedBotEvent(event)
                else -> dumpUnknownEvent(event)
            }
        }
    }

    private fun dumpUnknownEvent(event: Event) {
        append("Unknown event: ${event.javaClass.simpleName}", event.turnNumber, CssClass.ERROR)
    }

    private fun createEventAndTurnNumberStringBuilder(event: Event) =
        createEventNameStringBuilder(event).append(':').append("\n\t\tturnNumber:").append(event.turnNumber)

    private fun createEventNameStringBuilder(event: Event) =
        StringBuilder("\n\t" +
            when (event) {
                is BotDeathEvent -> if (bot.id == event.victimId) "DeathEvent" else "BotDeathEvent"
                is BulletHitBotEvent -> if (bot.id == event.victimId) "HitByBulletEvent" else "BulletHitBotEvent"
                else -> event.javaClass.simpleName
            }
        )

    private fun createVictimIDStringBuilder(event: Event, victimId: Int): StringBuilder {
        val sb = createEventAndTurnNumberStringBuilder(event)
        if (bot.id != victimId) {
            sb.append("\n\t\tvictimId:").append(victimId)
        }
        return sb
    }

    private fun dumpBotDeathEvent(botDeathEvent: BotDeathEvent) {
        dumpVictimIdOnly(botDeathEvent, botDeathEvent.victimId)
    }

    private fun dumpBotHitWallEvent(botHitWallEvent: BotHitWallEvent) {
        if (bot.id == botHitWallEvent.victimId) { // -> no need to dump victimId
            val sb = createEventAndTurnNumberStringBuilder(botHitWallEvent)
            append(sb.toString(), botHitWallEvent.turnNumber)
        }
    }

    private fun dumpVictimIdOnly(event: Event, victimId: Int) {
        if (bot.id != victimId) {
            val sb = createVictimIDStringBuilder(event, victimId)
            append(sb.toString(), event.turnNumber)
        }
    }

    private fun dumpBotHitBotEvent(botHitBotEvent: BotHitBotEvent) {
        if (botHitBotEvent.botId == bot.id) {
            val sb = createVictimIDStringBuilder(botHitBotEvent, botHitBotEvent.victimId)
                .append("\n\t\tenergy:").append(botHitBotEvent.energy)
                .append("\n\t\tx:").append(botHitBotEvent.x)
                .append("\n\t\ty:").append(botHitBotEvent.y)
                .append("\n\t\trammed:").append(botHitBotEvent.rammed)
            append(sb.toString(), botHitBotEvent.turnNumber)
        }
    }

    private fun dumpBulletFiredEvent(bulletFiredEvent: BulletFiredEvent) {
        dumpBulletOnly(bulletFiredEvent, bulletFiredEvent.bullet)
    }

    private fun dumpBulletHitBotEvent(bulletHitBotEvent: BulletHitBotEvent) {
        if (bulletHitBotEvent.bullet.ownerId == bot.id || bulletHitBotEvent.victimId == bot.id) {
            val sb = createVictimIDStringBuilder(bulletHitBotEvent, bulletHitBotEvent.victimId)
                .append("\n\t\tbullet:").appendBullet(bulletHitBotEvent.bullet)
                .append("\n\t\tdamage:").append(bulletHitBotEvent.damage)
                .append("\n\t\tenergy:").append(bulletHitBotEvent.energy)
            append(sb.toString(), bulletHitBotEvent.turnNumber)
        }
    }

    private fun StringBuilder.appendBullet(bullet: BulletState): StringBuilder {
        append("\n\t\t\tbulletId:").append(bullet.bulletId)
        if (bot.id != bullet.ownerId) {
            append("\n\t\t\townerId:").append(bullet.ownerId)
        }
        append("\n\t\t\tpower:").append(bullet.power)
            .append("\n\t\t\tx:").append(bullet.x)
            .append("\n\t\t\ty:").append(bullet.y)
            .append("\n\t\t\tdirection:").append(bullet.direction)
            .append("\n\t\t\tcolor:").append(bullet.color)
        return this
    }

    private fun dumpBulletHitBulletEvent(bulletHitBulletEvent: BulletHitBulletEvent) {
        val bullet = bulletHitBulletEvent.bullet
        val hitBullet = bulletHitBulletEvent.hitBullet

        if (bullet.ownerId == bot.id || hitBullet.ownerId == bot.id) {
            val sb = createEventAndTurnNumberStringBuilder(bulletHitBulletEvent)
                .append("\n\t\tbullet:").appendBullet(bullet)
                .append("\n\t\thitBullet:").appendBullet(hitBullet)
            append(sb.toString(), bulletHitBulletEvent.turnNumber)
        }
    }

    private fun dumpBulletHitWallEvent(bulletHitWallEvent: BulletHitWallEvent) {
        dumpBulletOnly(bulletHitWallEvent, bulletHitWallEvent.bullet)
    }

    private fun dumpBulletOnly(event: Event, bullet: BulletState) {
        if (bullet.ownerId == bot.id) {
            val sb = createEventAndTurnNumberStringBuilder(event)
                .append("\n\t\tbullet:").appendBullet(bullet)
            append(sb.toString(), event.turnNumber)
        }
    }

    private fun dumpScannedBotEvent(scannedBotEvent: ScannedBotEvent) {
        if (scannedBotEvent.scannedByBotId == bot.id) {
            val sb = createEventAndTurnNumberStringBuilder(scannedBotEvent)
                .append("\n\t\tscannedBotId:").append(scannedBotEvent.scannedBotId)
                .append("\n\t\tenergy:").append(scannedBotEvent.energy)
                .append("\n\t\tx:").append(scannedBotEvent.x)
                .append("\n\t\ty:").append(scannedBotEvent.y)
                .append("\n\t\tdirection:").append(scannedBotEvent.direction)
                .append("\n\t\tspeed:").append(scannedBotEvent.speed)
        }
    }

    private fun append(text: String, turnNumber: Int) {
        append(text.replace("\t", "  "), turnNumber, CssClass.NONE)
    }
}