package dev.robocode.tankroyale.common.event

import kotlin.reflect.KProperty

/**
 * Delegate for creating an [Event] instance using property delegation.
 *
 * This class enables the property delegation syntax for declaring events, reducing boilerplate
 * by automatically initializing the underlying `Event<T>` instance.
 *
 * ## Usage
 *
 * Instead of manually creating event properties:
 * ```kotlin
 * val onStarted = Event<StartEvent>()
 * ```
 *
 * Use property delegation:
 * ```kotlin
 * val onStarted by event<StartEvent>()
 * ```
 *
 * Both are equivalent; delegation is purely stylistic.
 *
 * @param T the event type managed by the delegated [Event]
 * @see event factory function to create instances
 * @see Event for the underlying event implementation
 */
class EventDelegate<T> {

    private val event = Event<T>()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Event<T> = event
}

/**
 * Creates an [EventDelegate] for property delegation: `val onEvent by event<T>()`.
 *
 * This factory function returns an `EventDelegate<T>` for Kotlin's property delegation syntax,
 * providing a convenient, zero-boilerplate way to declare event properties.
 *
 * ## Usage
 *
 * ```kotlin
 * object MyEvents {
 *     val onStarted by event<StartEvent>()
 *     val onStopped by event<StopEvent>()
 * }
 *
 * MyEvents.onStarted += On(this) { event -> println("Started: ${event.timestamp}") }
 * MyEvents.onStarted(StartEvent(System.currentTimeMillis()))
 * ```
 *
 * @param T the type of event to be managed by the returned delegate
 * @return an `EventDelegate<T>` instance for use with property delegation syntax
 *
 * @see EventDelegate for implementation details
 * @see Event for available methods and operators
 */
fun <T> event() = EventDelegate<T>()

