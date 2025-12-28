package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.gui.ansi.AnsiTextBuilder
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents

class BotEventsPanel(bot: Participant) : BaseBotConsolePanel(bot) {

    private val numberOfIndentionSpaces = 2

    init {
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        ClientEvents.apply {
            onTickEvent.subscribe(this@BotEventsPanel) { tickEvent ->
                if (isAlive(tickEvent) || hasJustDied(tickEvent)) {
                    dump(tickEvent)
                    tickEvent.events
                        .filter { it.turnNumber == tickEvent.turnNumber }
                        .forEach { dump(it) }
                }
            }
        }
    }

    private fun isAlive(tickEvent: TickEvent): Boolean {
        return tickEvent.botStates.any { it.id == bot.id && it.energy >= 0 }
    }

    private fun hasJustDied(tickEvent: TickEvent): Boolean =
        tickEvent.events.any { it is BotDeathEvent && it.victimId == bot.id && it.turnNumber == tickEvent.turnNumber }

    private fun dump(event: Event) {
        when (event) {
            is TickEvent -> dumpTickEvent(event)
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

    private fun dumpUnknownEvent(event: Event) {
        appendError("Unknown event: ${event.javaClass.simpleName}", event.turnNumber)
    }

    private fun createEventAndTurnNumberBuilder(event: Event) =
        createEventNameBuilder(event).text(':')
            .fieldValue("turnNumber", event.turnNumber)

    private fun createEventNameBuilder(event: Event) =
        AnsiTextBuilder().newline().space(numberOfIndentionSpaces).cyan().text(
            when (event) {
                is TickEvent -> "TickEvent"
                is BotDeathEvent -> if (bot.id == event.victimId) "DeathEvent" else "BotDeathEvent"
                is BulletHitBotEvent -> if (bot.id == event.victimId) "HitByBulletEvent" else "BulletHitBotEvent"
                else -> event.javaClass.simpleName
            }
        )

    private fun createVictimIdBuilder(event: Event, victimId: Int): AnsiTextBuilder {
        val ansi = createEventAndTurnNumberBuilder(event)
        if (bot.id != victimId) {
            ansi.fieldValue("victimId", botIdAndName(victimId))
        }
        return ansi
    }

    private fun AnsiTextBuilder.fieldValue(fieldName: String, value: Any?, indention: Int = 1): AnsiTextBuilder {
        newline().space((indention + 1) * numberOfIndentionSpaces).green().text(fieldName).text(": ").defaultColor()
            .bold().text(value).reset()
        return this
    }

    private fun AnsiTextBuilder.bulletValues(
        fieldName: String,
        bullet: BulletState,
        indention: Int = 1
    ): AnsiTextBuilder {
        val indent = indention + 1

        val bulletAnsi = AnsiTextBuilder()
            .fieldValue("bulletId", bullet.bulletId, indent)
        if (bot.id != bullet.ownerId) {
            bulletAnsi.fieldValue("ownerId", botIdAndName(bullet.ownerId), indent)
        }
        bulletAnsi.fieldValue("power", bullet.power, indent)
            .fieldValue("x", bullet.x, indent)
            .fieldValue("y", bullet.y, indent)
            .fieldValue("direction", bullet.direction, indent)
            .fieldValue("color", bullet.color, indent)

        fieldValue(fieldName, bulletAnsi.build(), indention)
        return this
    }

    private fun dumpTickEvent(tickEvent: TickEvent) {
        val ansi = createEventAndTurnNumberBuilder(tickEvent)
            .fieldValue("roundNumber", tickEvent.roundNumber)

        val bulletStates = tickEvent.bulletStates.filter { it.ownerId == bot.id }
        if (bulletStates.isNotEmpty()) {
            ansi.fieldValue("bulletStates", "")
            bulletStates.forEach { bulletState ->
                ansi.bulletValues("bulletState", bulletState, indention = 2)
            }
        }
        appendNewLine(ansi, tickEvent.turnNumber)
    }

    private fun dumpBotDeathEvent(botDeathEvent: BotDeathEvent) {
        dumpVictimIdOnly(botDeathEvent, botDeathEvent.victimId)
    }

    private fun dumpBotHitWallEvent(botHitWallEvent: BotHitWallEvent) {
        if (bot.id == botHitWallEvent.victimId) {
            val ansi = createEventAndTurnNumberBuilder(botHitWallEvent)
            appendNewLine(ansi, botHitWallEvent.turnNumber)
        }
    }

    private fun dumpVictimIdOnly(event: Event, victimId: Int) {
        val ansi = createVictimIdBuilder(event, victimId)
        appendNewLine(ansi, event.turnNumber)
    }

    private fun dumpBotHitBotEvent(botHitBotEvent: BotHitBotEvent) {
        if (botHitBotEvent.botId == bot.id || botHitBotEvent.victimId == bot.id) {
            val ansi = createVictimIdBuilder(botHitBotEvent, botHitBotEvent.victimId)
            if (botHitBotEvent.botId != bot.id) {
                ansi.fieldValue("botId", botIdAndName(botHitBotEvent.botId))
            }
            ansi.fieldValue("energy", botHitBotEvent.energy)
                .fieldValue("x", botHitBotEvent.x)
                .fieldValue("y", botHitBotEvent.y)
                .fieldValue("rammed", botHitBotEvent.rammed)
            appendNewLine(ansi, botHitBotEvent.turnNumber)
        }
    }

    private fun dumpBulletFiredEvent(bulletFiredEvent: BulletFiredEvent) {
        if (bulletFiredEvent.bullet.ownerId == bot.id) {
            val ansi = createEventAndTurnNumberBuilder(bulletFiredEvent)
                .bulletValues("bullet", bulletFiredEvent.bullet)
            appendNewLine(ansi, bulletFiredEvent.turnNumber)
        }
    }

    private fun dumpBulletHitBotEvent(bulletHitBotEvent: BulletHitBotEvent) {
        if (bulletHitBotEvent.bullet.ownerId == bot.id || bulletHitBotEvent.victimId == bot.id) {
            val ansi = createVictimIdBuilder(bulletHitBotEvent, bulletHitBotEvent.victimId)
                .bulletValues("bullet", bulletHitBotEvent.bullet)
                .fieldValue("damage", bulletHitBotEvent.damage)
                .fieldValue("energy", bulletHitBotEvent.energy)
            appendNewLine(ansi, bulletHitBotEvent.turnNumber)
        }
    }

    private fun dumpBulletHitBulletEvent(bulletHitBulletEvent: BulletHitBulletEvent) {
        val bullet = bulletHitBulletEvent.bullet
        val hitBullet = bulletHitBulletEvent.hitBullet

        if (bullet.ownerId == bot.id || hitBullet.ownerId == bot.id) {
            val ansi = createEventAndTurnNumberBuilder(bulletHitBulletEvent)
                .bulletValues("bullet", bullet)
                .bulletValues("hitBullet", hitBullet)
            appendNewLine(ansi, bulletHitBulletEvent.turnNumber)
        }
    }

    private fun dumpBulletHitWallEvent(bulletHitWallEvent: BulletHitWallEvent) {
        if (bulletHitWallEvent.bullet.ownerId == bot.id) {
            val ansi = createEventAndTurnNumberBuilder(bulletHitWallEvent)
                .bulletValues("bullet", bulletHitWallEvent.bullet)
            appendNewLine(ansi, bulletHitWallEvent.turnNumber)
        }
    }

    private fun dumpScannedBotEvent(scannedBotEvent: ScannedBotEvent) {
        if (scannedBotEvent.scannedByBotId == bot.id) {
            val ansi = createEventAndTurnNumberBuilder(scannedBotEvent)
                .fieldValue("scannedBotId", botIdAndName(scannedBotEvent.scannedBotId))
                .fieldValue("energy", scannedBotEvent.energy)
                .fieldValue("x", scannedBotEvent.x)
                .fieldValue("y", scannedBotEvent.y)
                .fieldValue("direction", scannedBotEvent.direction)
                .fieldValue("speed", scannedBotEvent.speed)
            appendNewLine(ansi, scannedBotEvent.turnNumber)
        }
    }

    private fun botIdAndName(botId: Int): String {
        val bot = Client.getParticipant(botId)
        return "$botId (${bot.name} ${bot.version})"
    }

    private fun appendNewLine(ansiTextBuilder: AnsiTextBuilder, turnNumber: Int? = null) {
        append(ansiTextBuilder.newline().build(), turnNumber)
    }
}
