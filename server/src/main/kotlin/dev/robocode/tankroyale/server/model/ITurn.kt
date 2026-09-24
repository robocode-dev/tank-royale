package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.event.Event

/** Mutable state of a game turn in a round. */
interface ITurn {
    /** Turn number */
    val turnNumber: Int

    /** Bots */
    val bots: Set<IBot>

    /** Bullets */
    val bullets: Set<Bullet>

    /** Observer events  */
    val observerEvents: Set<Event>

    /** Ordered events for each bot. Duplicates are significant for team messages. */
    val botEvents: Map<BotId, List<Event>>

    /**
     * Returns a bot instance by id.
     * @param botId is the id of the bot.
     * @return the bot instance with the specified id or null if the bot was not found.
     */
    fun getBot(botId: BotId): IBot? = bots.find { it.id == botId }

    /**
     * Returns the event for a specific bot.
     * @param botId is the id of the bot.
     * @return ordered bot events, including duplicates.
     */
    fun getEvents(botId: BotId): List<Event> = botEvents[botId] ?: emptyList()
}
