package dev.robocode.tankroyale.common.event

/**
 * Sealed class representing event subscription types: [On] (continuous) or [Once] (one-shot).
 *
 * Enables type-safe discrimination between subscription variants at compile-time and runtime.
 *
 * @param T the event type
 * @param owner the owner (typically `this`), used as the weak reference key
 * @param priority optional execution order (higher = earlier). Default is 0.
 * @param handler the event handler lambda
 *
 * @see On for continuous subscriptions
 * @see Once for one-shot subscriptions
 */
sealed class Subscription<T>(
    open val owner: Any,
    open val priority: Int = 0,
    open val handler: (T) -> Unit
)

/**
 * Continuous event subscription wrapper for the `+=` operator.
 *
 * Handlers remain subscribed until explicitly unsubscribed (via `-=`) or their owner is garbage collected.
 *
 * ## Usage
 *
 * ```kotlin
 * myEvent += On(this) { event -> println("Event: $event") }
 * myEvent += On(this, priority = 100) { event -> println("Priority: $event") }
 * ```
 *
 * Higher priority values execute first (default is 0).
 *
 * @param T the event type
 * @param owner the owner (typically `this`)
 * @param priority optional execution order (higher = earlier). Default is 0.
 * @param handler the event handler lambda
 *
 * @see Event.plusAssign for subscription processing
 * @see Once for one-shot subscriptions
 */
data class On<T>(
    override val owner: Any,
    override val priority: Int = 0,
    override val handler: (T) -> Unit
) : Subscription<T>(owner, priority, handler)


/**
 * One-shot event subscription wrapper for the `+=` operator.
 *
 * Handlers are automatically unsubscribed after receiving the first event.
 *
 * ## Usage
 *
 * ```kotlin
 * // Subscribe to receive exactly one event, then auto-unsubscribe
 * myEvent += Once(this) { event -> println("Received once: $event") }
 *
 * // With custom priority
 * myEvent += Once(this, priority = 100) { event -> println("Priority once: $event") }
 * ```
 *
 * @param T the event type
 * @param owner the owner (typically `this`)
 * @param priority optional execution order (higher = earlier). Default is 0.
 * @param handler the event handler lambda
 *
 * @see Event.plusAssign for subscription processing
 * @see On for continuous subscriptions
 */
data class Once<T>(
    override val owner: Any,
    override val priority: Int = 0,
    override val handler: (T) -> Unit
) : Subscription<T>(owner, priority, handler)

