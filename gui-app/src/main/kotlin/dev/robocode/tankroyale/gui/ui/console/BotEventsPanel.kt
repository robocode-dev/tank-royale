package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.ansi.AnsiEscapeCode
import dev.robocode.tankroyale.gui.ansi.AnsiTextBuilder
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.*

class BotEventsPanel(bot: Participant) : BaseBotConsolePanel(bot) {

    private val numberOfIndentionSpaces = 2

    init {
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        ClientEvents.apply {
            onTickEvent.subscribe(this@BotEventsPanel) { dumpTickEvent(it) }

            onGameAborted.unsubscribe {
                onTickEvent.unsubscribe(this@BotEventsPanel)
            }
            onGameEnded.unsubscribe {
                onTickEvent.unsubscribe(this@BotEventsPanel)
            }
        }
    }

    private fun dumpTickEvent(tickEvent: TickEvent) {
        val ansi = createEventAndTurnNumberBuilder(tickEvent)
            .fieldValue("roundNumber", tickEvent.roundNumber)
//            .fieldValue("enemyCount", // TODO
            .botValues("botState", tickEvent.botStates.first { bot.id == it.id })

        val bullets = tickEvent.bulletStates.filter { bot.id == it.ownerId }.toList()
        ansi.fieldValue("bulletStates", "")
        bullets.forEach { bullet ->
            ansi.bulletValues(null, bullet)
        }
        append(ansi.toString(), tickEvent.turnNumber)
    }

    private fun dumpEvents(events: Set<Event>) {
        events.forEach { event ->
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
    }

    private fun dumpUnknownEvent(event: Event) {
        append("Unknown event: ${event.javaClass.simpleName}", event.turnNumber, CssClass.ERROR)
    }

    private fun dumpBotDeathEvent(botDeathEvent: BotDeathEvent) {
        dumpVictimIdOnly(botDeathEvent, botDeathEvent.victimId)
    }

    private fun dumpBotHitWallEvent(botHitWallEvent: BotHitWallEvent) {
        if (bot.id == botHitWallEvent.victimId) { // -> no need to dump victimId
            val ansi = createEventAndTurnNumberBuilder(botHitWallEvent)
            append(ansi.toString(), botHitWallEvent.turnNumber)
        }
    }

    private fun dumpVictimIdOnly(event: Event, victimId: Int) {
        if (bot.id != victimId) {
            val ansi = createVictimIdBuilder(event, victimId)
            append(ansi.toString(), event.turnNumber)
        }
    }

    private fun dumpBotHitBotEvent(botHitBotEvent: BotHitBotEvent) {
        if (botHitBotEvent.botId == bot.id) {
            val ansi = createVictimIdBuilder(botHitBotEvent, botHitBotEvent.victimId)
                .fieldValue("energy", botHitBotEvent.energy)
                .fieldValue("x", botHitBotEvent.x)
                .fieldValue("y", botHitBotEvent.y)
                .fieldValue("rammed", botHitBotEvent.rammed)
            append(ansi.toString(), botHitBotEvent.turnNumber)
        }
    }

    private fun dumpBulletFiredEvent(bulletFiredEvent: BulletFiredEvent) {
        dumpBulletOnly(bulletFiredEvent, bulletFiredEvent.bullet)
    }

    private fun dumpBulletHitBotEvent(bulletHitBotEvent: BulletHitBotEvent) {
        if (bulletHitBotEvent.bullet.ownerId == bot.id || bulletHitBotEvent.victimId == bot.id) {
            val ansi = createVictimIdBuilder(bulletHitBotEvent, bulletHitBotEvent.victimId)
                .bulletValues("bullet", bulletHitBotEvent.bullet)
                .fieldValue("damage", bulletHitBotEvent.damage)
                .fieldValue("energy", bulletHitBotEvent.energy)
            append(ansi.toString(), bulletHitBotEvent.turnNumber)
        }
    }

    private fun dumpBulletHitBulletEvent(bulletHitBulletEvent: BulletHitBulletEvent) {
        val bullet = bulletHitBulletEvent.bullet
        val hitBullet = bulletHitBulletEvent.hitBullet

        if (bullet.ownerId == bot.id || hitBullet.ownerId == bot.id) {
            val ansi = createEventAndTurnNumberBuilder(bulletHitBulletEvent)
                .bulletValues("bullet", bullet)
                .bulletValues("hitBullet", hitBullet)
            append(ansi.toString(), bulletHitBulletEvent.turnNumber)
        }
    }

    private fun dumpBulletHitWallEvent(bulletHitWallEvent: BulletHitWallEvent) {
        dumpBulletOnly(bulletHitWallEvent, bulletHitWallEvent.bullet)
    }

    private fun dumpBulletOnly(event: Event, bullet: BulletState) {
        if (bullet.ownerId == bot.id) {
            val ansi = createEventAndTurnNumberBuilder(event)
                .bulletValues("bullet", bullet)
            append(ansi.toString(), event.turnNumber)
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
            append(ansi.toString(), scannedBotEvent.turnNumber)
        }
    }

    private fun createEventAndTurnNumberBuilder(event: Event) =
        createEventNameBuilder(event).text(':')
            .fieldValue("turnNumber", event.turnNumber)

    private fun createEventNameBuilder(event: Event) =
        AnsiTextBuilder().newline().space(numberOfIndentionSpaces).esc(AnsiEscapeCode.CYAN).text(
            when (event) {
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

    private fun AnsiTextBuilder.indent(text: Any, indention: Int = 3): AnsiTextBuilder {
        newline().space(indention * numberOfIndentionSpaces).green().text(text).text(": ")
        return this
    }

    private fun AnsiTextBuilder.fieldValue(fieldName: String?, value: Any?, indention: Int = 2): AnsiTextBuilder {
        if (fieldName != null) {
            indent(fieldName, indention).default().bold().text(value).reset()
        }
        return this
    }

    private fun AnsiTextBuilder.botValues(fieldName: String, botState: BotState, indention: Int = 2): AnsiTextBuilder {
        val indent = indention + 1
        val botAnsi = AnsiTextBuilder()
        if (bot.id != botState.id) {
            fieldValue("id", botIdAndName(botState.id))
        }
        botAnsi
            .fieldValue("energy", botState.energy, indent)
            .fieldValue("x", botState.x, indent)
            .fieldValue("y", botState.y, indent)
            .fieldValue("direction", botState.direction, indent)
            .fieldValue("gunDirection", botState.gunDirection, indent)
            .fieldValue("radarDirection", botState.radarDirection, indent)
            .fieldValue("radarSweep", botState.radarSweep, indent)
            .fieldValue("speed", botState.speed, indent)
            .fieldValue("turnRate", botState.turnRate, indent)
            .fieldValue("gunTurnRate", botState.gunTurnRate, indent)
            .fieldValue("radarTurnRate", botState.radarTurnRate, indent)
            .fieldValue("gunHeat", botState.gunHeat, indent)
            .fieldValue("bodyColor", botState.bodyColor, indent)
            .fieldValue("turretColor", botState.turretColor, indent)
            .fieldValue("radarColor", botState.radarColor, indent)
            .fieldValue("bulletColor", botState.bulletColor, indent)
            .fieldValue("scanColor", botState.scanColor, indent)
            .fieldValue("tracksColor", botState.tracksColor, indent)
            .fieldValue("gunColor", botState.gunColor, indent)
//            .fieldValue("stdOut", botState.stdOut, indent) // TODO: must be escaped \t \r \n
//            .fieldValue("stdErr", botState.stdErr, indent)  // TODO: must be escaped \t \r \n

        fieldValue(fieldName, botAnsi.toString(), indention)
        return this
    }

    private fun AnsiTextBuilder.bulletValues(fieldName: String?, bullet: BulletState, indention: Int = 2): AnsiTextBuilder {
        val indent = indention + 1
        val bulletAnsi = AnsiTextBuilder()

        if (fieldName != null) {
            bulletAnsi.fieldValue("bulletId", bullet.bulletId, indent)
        } else {
            bulletAnsi.fieldValue("- bulletId", bullet.bulletId, indent - 1)
        }
        if (bot.id != bullet.ownerId) {
            bulletAnsi.fieldValue("ownerId", botIdAndName(bullet.ownerId), indent)
        }
        bulletAnsi
            .fieldValue("power", bullet.power, indent)
            .fieldValue("x", bullet.x, indent)
            .fieldValue("y", bullet.y, indent)
            .fieldValue("direction", bullet.direction, indent)
            .fieldValue("color", bullet.color, indent)

        if (fieldName != null) {
            fieldValue(fieldName, bulletAnsi.toString(), indention)
        } else {
            append(bulletAnsi.toString())
        }
        return this
    }

    private fun botIdAndName(botId: Int): String {
        val bot = Client.getParticipant(botId)
        return "$botId (${bot.name} ${bot.version})"
    }
}