package dev.robocode.tankroyale.common

import kotlin.reflect.KProperty

/**
 * Delegate for creating an [Event] instance using property delegation.
 *
 * This class enables the property delegation syntax for declaring events, which reduces boilerplate
 * by automatically initializing the underlying `Event<T>` instance.
 *
 * ## Usage
 *
 * Instead of manually creating event properties:
 *
 * ```kotlin
 * object MyEvents {
 *     val onStarted = Event<StartEvent>()
 *     val onStopped = Event<StopEvent>()
 * }
 * ```
 *
 * Use property delegation with the `event()` factory:
 *
 * ```kotlin
 * object MyEvents {
 *     val onStarted by event<StartEvent>()
 *     val onStopped by event<StopEvent>()
 * }
 * ```
 *
 * Both approaches are equivalent. Delegation is **optional** and purely stylistic; it does not
 * provide additional functionality, only syntactic convenience.
 *
 * ## How It Works
 *
 * - The `by event<T>()` syntax invokes [event] factory function, returning an `EventDelegate<T>`
 * - When the property is accessed, `getValue()` is called, returning the underlying `Event<T>` instance
 * - Subsequent accesses return the same instance (the event is created once during initialization)
 *
 * ## Example
 *
 * ```kotlin
 * object Events {
 *     val onStart by event<StartEvent>()
 * }
 *
 * // First access creates Event<StartEvent> and returns it
 * Events.onStart.subscribe(this) { event -> println("Started") }
 *
 * // Same instance is reused
 * Events.onStart.fire(StartEvent())
 * ```
 *
 * ## Notes
 *
 * - Purely stylistic; `val event = Event<T>()` is equally valid
 * - Particularly useful in event-heavy objects with many properties
 * - All [Event] methods and operators work identically with delegated properties
 * - No performance overhead compared to manual initialization
 *
 * @param T the event type managed by the delegated [Event]
 *
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
 * This factory function returns an `EventDelegate<T>` that can be used with Kotlin's property
 * delegation syntax. It provides a convenient, zero-boilerplate way to declare event properties.
 *
 * ## Usage
 *
 * ```kotlin
 * object MyEvents {
 *     val onStarted by event<StartEvent>()
 *     val onStopped by event<StopEvent>()
 *     val onProgress by event<ProgressEvent>()
 * }
 *
 * // Subscribe to events
 * MyEvents.onStarted += On(this) { event -> println("Started: ${event.timestamp}") }
 * MyEvents.onProgress += On(this) { event -> println("Progress: ${event.percent}%") }
 * MyEvents.onStopped += On(this) { event -> println("Stopped") }
 *
 * // Fire events
 * MyEvents.onStarted(StartEvent(System.currentTimeMillis()))
 * MyEvents.onProgress(ProgressEvent(50))
 * MyEvents.onStopped(StopEvent())
 * ```
 *
 * @param T the type of event to be managed by the returned delegate
 * @return an `EventDelegate<T>` instance for use with property delegation syntax
 *
 * @see EventDelegate for implementation details
 * @see Event for available methods and operators
 */
fun <T> event() = EventDelegate<T>()

