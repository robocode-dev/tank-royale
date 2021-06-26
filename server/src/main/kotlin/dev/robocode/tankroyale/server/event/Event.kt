package dev.robocode.tankroyale.server.event

/** Bot event super class. */
abstract class Event {
    /** Turn number when event occurred */
    abstract val turnNumber: Int
}