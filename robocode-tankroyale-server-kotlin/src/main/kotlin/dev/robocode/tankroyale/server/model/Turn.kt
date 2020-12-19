package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.event.Event
import kotlin.collections.HashSet

/** State of a game turn in a round. */
data class Turn(
    /** Turn number */
    var turnNumber: Int,

    /** Bots */
    val bots: MutableSet<IBot> = mutableSetOf(),

    /** Bullets */
    val bullets: MutableSet<IBullet> = mutableSetOf(),

    /** Observer events  */
    val observerEvents: MutableSet<Event> = mutableSetOf(),

    /** Map over bot events  */
    private val botEventsMap: MutableMap<BotId, MutableSet<Event>> = mutableMapOf(),
) {
    /**
     * Replaces all bullets with a collection of bullet copies.
     * @param srcBullets is the collection of bullets to copy.
     */
    fun copyBullets(srcBullets: Collection<IBullet>) {
        bullets.clear()
        srcBullets.forEach { bullet -> bullets += bullet }
    }

    /**
     * Replaces all bots with a collection of bot copies.
     * @param srcBots is the collection of bots to copy.
     */
    fun copyBots(srcBots: Collection<IBot>) {
        bots.clear()
        srcBots.forEach { bot -> bots += bot }
    }

    /**
     * Returns a bot instance by id.
     * @param botId is the id of the bot.
     * @return the bot instance with the specified id or null if the bot was not found.
     */
    fun getBot(botId: BotId): IBot? = bots.find { it.id == botId }

    /**
     * Returns the event for a specific bot.
     * @param botId is the id of the bot.
     * @return a set of bot events.
     */
    fun getEvents(botId: BotId): Set<Event> = botEventsMap[botId] ?: HashSet()

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
        var botEvents: MutableSet<Event>? = botEventsMap[botId]
        if (botEvents == null) {
            botEvents = HashSet()
            botEventsMap[botId] = botEvents
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
        botEventsMap.clear()
        observerEvents.clear()
    }
}