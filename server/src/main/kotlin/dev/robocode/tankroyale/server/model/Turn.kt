package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.event.Event

/** Mutable state of a game turn in a round. */
data class Turn(
    /** Turn number */
    override val turnNumber: Int,

    /** Bots */
    override val bots: Set<IBot>,

    /** Bullets */
    override val bullets: Set<IBullet>,

    /** Observer events  */
    override val observerEvents: Set<Event>,

    /** Map over bot events  */
    override val botEvents: Map<BotId, Set<Event>>,

    ) : ITurn