package dev.robocode.tankroyale.common

/**
 * Wrapper for one-shot event subscriptions using the `+=` operator.
 *
 * This data class enables concise one-shot event subscriptions using the `+=` operator.
 * When combined with the [Event.plusAssign] operator, it allows subscribing to receive
 * exactly one event before automatic unsubscription.
 *
 * ## Usage
 *
 * ```kotlin
 * // Subscribe to receive exactly one event, then auto-unsubscribe
 * myEvent += Once(this) { event -> println("Received once: $event") }
 * ```
 *
 * Equivalent to:
 *
 * ```kotlin
 * myEvent.subscribe(this, once = true) { event -> println("Received once: $event") }
 * ```
 *
 * ## How It Works
 *
 * - The owner receives the first event
 * - Immediately after delivery, the handler is automatically unsubscribed
 * - Subsequent fires will not invoke the handler
 * - Garbage collection is still automatic (weak reference semantics apply)
 *
 * ## Examples
 *
 * ### Initialization-Complete Event
 *
 * ```kotlin
 * class MyComponent : JPanel() {
 *     init {
 *         // Initialize on first game start, then ignore subsequent starts
 *         GameEvents.onStarted += Once(this) { event ->
 *             initializeUI()
 *         }
 *     }
 * }
 * ```
 *
 * ### Completion Callback
 *
 * ```kotlin
 * // Wait for exactly one completion signal
 * myEvent += Once(this) { event ->
 *     println("Operation completed: $event")
 *     // Future fires will not invoke this handler
 * }
 * ```
 *
 * @param T the event type
 * @param owner the owner (typically `this`), used as the weak reference key
 * @param handler the event handler lambda to invoke on the first event
 *
 * @see Event.plusAssign for the operator that processes this wrapper
 * @see Event.subscribe with `once=true` for the method-based equivalent
 */
data class Once<T>(val owner: Any, val handler: (T) -> Unit)

