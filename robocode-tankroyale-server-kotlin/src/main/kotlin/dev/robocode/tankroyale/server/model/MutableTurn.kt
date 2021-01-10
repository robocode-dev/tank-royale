package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.event.Event
import kotlin.collections.HashSet

/** Mutable state of a game turn in a round. */
data class MutableTurn(
    /** Turn number */
    override var turnNumber: Int,

    /** Bots */
    override val bots: MutableSet<IBot> = mutableSetOf(),

    /** Bullets */
    override val bullets: MutableSet<IBullet> = mutableSetOf(),

    /** Observer events  */
    override val observerEvents: MutableSet<Event> = mutableSetOf(),

    /** Map over bot events  */
    override val botEvents: MutableMap<BotId, MutableSet<Event>> = mutableMapOf(),

) : ITurn {

    /** Returns an immutable copy of this turn */
    fun toTurn() = Turn(turnNumber, copyBots(), copyBullets(), observerEvents.toSet(), copyBotEvents())

    /** Returns a deep copy of the bots */
    private fun copyBots(): Set<IBot> {
        val botsCopy = mutableSetOf<IBot>()
        bots.forEach { bot -> botsCopy += copyBot(bot) }
        return botsCopy.toSet()
    }

    /** Returns a deep copy of the bullets */
    private fun copyBullets(): Set<IBullet> {
        val bulletsCopy = mutableSetOf<IBullet>()
        bullets.forEach { bullet -> bulletsCopy += copyBullet(bullet) }
        return bulletsCopy.toSet()
    }

    /** Returns a deep copy of the bot events */
    private fun copyBotEvents(): Map<BotId, Set<Event>> {
        val botEventsCopy = mutableMapOf<BotId, Set<Event>>()
        botEvents.forEach { (botId, events) -> run {
            botEventsCopy[botId] = events.toSet()
        }}
        return botEventsCopy.toMap()
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

    /** Deep copies a bot */
    private fun copyBot(bot: IBot): IBot {
        return Bot(
            bot.id,
            bot.energy,
            Point(bot.position.x, bot.position.y),
            bot.direction,
            bot.gunDirection,
            bot.scanDirection,
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
            bot.scanDirection,
            bot.scanSpreadAngle
        )
    }

    /** Deep copies a bullet */
    private fun copyBullet(bullet: IBullet): IBullet {
        return Bullet(
            bullet.id,
            bullet.botId,
            bullet.power,
            bullet.direction,
            bullet.color,
            bullet.startPosition,
            bullet.tick,
        )
    }

    /**
     * Adds an observer event.
     * @param event is the observer event to add.
     */
    fun addObserverEvent(event: Event) { observerEvents += event }

    /**
     * Adds a private bot event.
     * @param botId is the bot id.
     * @param event is the bot event, only given to the specified bot.
     */
    fun addPrivateBotEvent(botId: BotId, event: Event) {
        // Only a specific bot retrieves the event, not any other bot
        var botEvents: MutableSet<Event>? = botEvents[botId]
        if (botEvents == null) {
            botEvents = HashSet()
            this.botEvents[botId] = botEvents
        }
        botEvents.add(event)
    }

    /**
     * Adds a public bot event.
     * @param event is the bot event.
     */
    fun addPublicBotEvent(event: Event) {
        // Every bots get notified about the bot event
        bots.forEach { addPrivateBotEvent(it.id, event) }
    }

    /** Reset all events. */
    fun resetEvents() {
        botEvents.clear()
        observerEvents.clear()
    }
}