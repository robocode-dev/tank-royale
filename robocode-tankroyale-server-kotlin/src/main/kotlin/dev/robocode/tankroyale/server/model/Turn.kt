package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.event.Event
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/** State of a game turn in a round. */
class Turn(
    /** Turn number */
    var turnNumber: Int,

    /** Bots */
    private val bots: MutableSet<Bot> = HashSet(),

    /** Bullets */
    private val bullets: MutableSet<Bullet> = HashSet(),

    /** Observer events  */
    private val observerEvents: MutableSet<Event> = HashSet(),

    /** Map over bot events  */
    private val botEventsMap: MutableMap<Int, Set<Event>> = HashMap(),
) {
    /**
     * Returns a bot instance by id.
     * @param botId is the id of the bot.
     * @return the bot instance with the specified id or null if the bot was not found.
     */
    fun botById(botId: Int): Bot? = bots.find { it.id == botId }

    /**
     * Returns the bullets fired by a specific bot.
     * @param botId is the id of the bot that fired the bullets.
     * @return a set of bullets.
     */
    fun bulletsByBotId(botId: Int): List<Bullet> = bullets.filter { it.botId == botId }

    /**
     * Returns the event for a specific bot.
     * @param botId is the id of the bot.
     * @return a set of bot events.
     */
    fun eventsByBotId(botId: Int): Set<Event>? = botEventsMap[botId]

    /**
     * Adds an observer event.
     * @param event is the observer event to add.
     */
    fun addObserverEvent(event: Event) {
        observerEvents + event
    }

    /**
     * Adds a private bot event.
     * @param botId is the bot id.
     * @param event is the bot event, only given to the specified bot.
     */
    fun addPrivateBotEvent(botId: Int, event: Event) {
        // Only a specific bot retrieves the event, not any other bot
        botEventsMap[botId] = (eventsByBotId(botId) ?: HashSet()) + event
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