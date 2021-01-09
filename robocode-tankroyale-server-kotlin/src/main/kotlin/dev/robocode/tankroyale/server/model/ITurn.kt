package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.event.Event

/** Mutable state of a game turn in a round. */
interface ITurn {
    /** Turn number */
    val turnNumber: Int

    /** Bots */
    val bots: Set<IBot>

    /** Bullets */
    val bullets: Set<IBullet>

    /** Observer events  */
    val observerEvents: Set<Event>

    /** Map over bot events  */
    val botEvents: Map<BotId, Set<Event>>

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
    fun getEvents(botId: BotId): Set<Event> = botEvents[botId] ?: HashSet()
}
