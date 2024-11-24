package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.event.Event

/** Mutable state of a game turn in a round. */
data class MutableTurn(
    /** Turn number */
    override var turnNumber: Int,

    /** Bots */
    override val bots: MutableSet<IBot> = mutableSetOf(),

    /** Bullets */
    override val bullets: MutableSet<IBullet> = mutableSetOf(),

    /** Map over bot events  */
    override val botEvents: MutableMap<BotId, MutableSet<Event>> = mutableMapOf(),

    /** Observer events  */
    override val observerEvents: MutableSet<Event> = mutableSetOf(),

    ) : ITurn {

    /** Returns an immutable copy of this turn */
    fun toTurn() = Turn(turnNumber, copyBots(), copyBullets(), observerEvents.toSet(), copyBotEvents())

    /**
     * Adds an observer event.
     * @param event is the observer event to add.
     */
    fun addObserverEvent(event: Event) {
        observerEvents += event
    }

    /**
     * Adds a private bot event to for a specific bot.
     * @param botId is the bot id.
     * @param event is the bot event, only given to the specified bot.
     */
    fun addPrivateBotEvent(botId: BotId, event: Event) {
        botEvents.getOrPut(botId) { HashSet() }.add(event)
    }

    /**
     * Adds a public bot event to every bot.
     * @param event is the bot event.
     */
    fun addPublicBotEvent(event: Event) {
        bots.forEach { addPrivateBotEvent(it.id, event) }
    }

    /** Reset all events. */
    fun resetEvents() {
        botEvents.clear()
        observerEvents.clear()
    }

    /** Returns a deep copy of the bots */
    private fun copyBots(): Set<IBot> {
        return bots.map { copyBot(it) }.toSet()
    }

    /** Returns a deep copy of the bullets */
    private fun copyBullets(): Set<IBullet> {
        return bullets.map { copyBullet(it) }.toSet()
    }

    /** Returns a deep copy of the bot events */
    private fun copyBotEvents(): Map<BotId, Set<Event>> {
        return botEvents.mapValues { (_, events) -> events.toSet() }
    }

    /**
     * Replaces all bullets with a collection of bullet copies.
     * @param srcBullets is the collection of bullets to copy.
     */
    fun copyBullets(srcBullets: Collection<IBullet>) {
        bullets.clear()
        srcBullets.forEach { bullet -> bullets += copyBullet(bullet) }
    }

    /**
     * Replaces all bots with a collection of bot copies.
     * @param srcBots is the collection of bots to copy.
     */
    fun copyBots(srcBots: Collection<IBot>) {
        bots.clear()
        srcBots.forEach { bot -> bots += copyBot(bot) }
    }

    companion object {

        /** Deep copies a bot */
        private fun copyBot(bot: IBot): IBot =
            Bot(
                bot.id,
                bot.isDroid,
                bot.sessionId,
                bot.energy,
                Point(bot.position.x, bot.position.y),
                bot.direction,
                bot.gunDirection,
                bot.radarDirection,
                bot.radarSpreadAngle,
                bot.speed,
                bot.turnRate,
                bot.gunTurnRate,
                bot.radarTurnRate,
                bot.gunHeat,
                bot.bodyColor,
                bot.turretColor,
                bot.radarColor,
                bot.bulletColor,
                bot.scanColor,
                bot.tracksColor,
                bot.gunColor,
                bot.stdOut,
                bot.stdErr,
                bot.isDebuggingEnabled,
                bot.debugGraphics,
            )

        /** Deep copies a bullet */
        private fun copyBullet(bullet: IBullet): IBullet =
            Bullet(
                bullet.id,
                bullet.botId,
                bullet.power,
                bullet.direction,
                bullet.color,
                bullet.startPosition,
                bullet.tick,
            )
    }
}