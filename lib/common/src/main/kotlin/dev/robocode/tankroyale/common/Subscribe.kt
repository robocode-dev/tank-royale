package dev.robocode.tankroyale.common

/**
 * Wrapper for explicit, clear event subscriptions using the `+=` operator.
 *
 * ## Purpose
 *
 * Provides more readable and explicit syntax compared to the `this to handler` infix notation.
 * While both are functionally equivalent, `Subscribe` makes the intent clearer at call sites.
 *
 * ## Usage
 *
 * ```kotlin
 * // Instead of the less clear:
 * myEvent += this to { event -> println("Event: $event") }
 *
 * // You can write (recommended):
 * myEvent += Subscribe(this) { event -> println("Event: $event") }
 * ```
 *
 * Both are equivalent to:
 * ```kotlin
 * myEvent.subscribe(this) { event -> println("Event: $event") }
 * ```
 *
 * ## When to Use
 *
 * - **Recommended** when subscription syntax is preferred over method calls
 * - **Clearer** than `this to` for developers unfamiliar with Kotlin infix functions
 * - **Consistent** with [Once] wrapper for visual consistency
 * - Use regular `.subscribe()` method when you don't need operator syntax
 *
 * ## Example: Menu Event Handler
 *
 * ```kotlin
 * object MenuEventHandlers {
 *     init {
 *         MenuEventTriggers.apply {
 *             onStartBattle += Subscribe(this) {
 *                 startBattle()
 *             }
 *             onHelp += Subscribe(this) {
 *                 Browser.browse(HELP_URL)
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param T the event type
 * @param owner the owner (typically `this`), used as the weak reference key
 * @param handler the event handler lambda to invoke on each event
 *
 * @see Event.plusAssign for the operator that processes this wrapper
 * @see Event.subscribe for the method-based equivalent
 * @see Once for a one-shot subscription wrapper
 */
data class Subscribe<T>(val owner: Any, val handler: (T) -> Unit)

