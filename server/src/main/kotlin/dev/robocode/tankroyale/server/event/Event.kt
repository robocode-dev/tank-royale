package dev.robocode.tankroyale.server.event

/** Bot event super class. */
sealed class Event {
    /** Turn number when event occurred */
    abstract val turnNumber: Int
}
