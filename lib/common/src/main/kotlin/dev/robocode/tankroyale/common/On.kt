package dev.robocode.tankroyale.common

/**
 * Wrapper for continuous event subscriptions using the `+=` operator.
 *
 * ## Purpose
 *
 * Provides readable and explicit syntax for subscribing to events continuously.
 * Short and memorable name (`On`) pairs naturally with [Once] for one-shot subscriptions.
 *
 * ## Usage
 *
 * ```kotlin
 * // Basic subscription with default priority:
 * myEvent += On(this) { event -> println("Event: $event") }
 *
 * // Subscription with custom priority (higher = earlier execution):
 * myEvent += On(this, priority = 100) { event -> println("Priority event: $event") }
 *
 * // Alternative (less clear):
 * myEvent += this to { event -> println("Event: $event") }
 *
 * // Equivalent method-based syntax:
 * myEvent.subscribe(this) { event -> println("Event: $event") }
 * myEvent.subscribe(this, priority = 100) { event -> println("Event: $event") }
 * ```
 *
 * ## When to Use
 *
 * - **Recommended** when subscription syntax is preferred over method calls
 * - **Clearer** than `this to` for developers unfamiliar with Kotlin infix functions
 * - **Consistent** with [Once] wrapper for visual symmetry (`On` for continuous, `Once` for one-shot)
 * - Use `priority` parameter when handler execution order matters
 * - Use regular `.subscribe()` method when you don't need operator syntax
 *
 * ## Priority Handling
 *
 * Higher priority values execute first. Default priority is 0.
 *
 * ```kotlin
 * myEvent += On(this, priority = 100) { ... }  // Executes first
 * myEvent += On(this, priority = 50) { ... }   // Executes second
 * myEvent += On(this) { ... }                  // Executes third (default priority 0)
 * ```
 *
 * ## Example: Menu Event Handler
 *
 * ```kotlin
 * object MenuEventHandlers {
 *     init {
 *         MenuEventTriggers.apply {
 *             onStartBattle += On(this) {
 *                 startBattle()
 *             }
 *             onHelp += On(this) {
 *                 Browser.browse(HELP_URL)
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param T the event type
 * @param owner the owner (typically `this`), used as the weak reference key
 * @param priority optional priority for event handler execution order (higher = earlier). Default is 0.
 * @param handler the event handler lambda to invoke on each event
 *
 * @see Event.plusAssign for the operator that processes this wrapper
 * @see Event.subscribe for the method-based equivalent
 * @see Once for a one-shot subscription wrapper
 */
data class On<T>(
    val owner: Any,
    val priority: Int = 0,
    val handler: (T) -> Unit
)

