package dev.robocode.tankroyale.common

import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * A thread-safe, type-safe event system using weak references to prevent memory leaks.
 *
 * This event system provides C#/.NET-style delegates for Java/Kotlin, allowing decoupled event-driven
 * architectures without explicit listener interfaces or manual cleanup. Event handlers are automatically
 * unsubscribed when their owners are garbage collected.
 *
 * ## Thread-Safety
 *
 * Fully thread-safe with atomic, lock-free semantics:
 * - **Subscribe/Unsubscribe**: Synchronized map for safe registration/removal across threads
 * - **Fire Operations**: Lock-free reads via atomic snapshot prevent `ConcurrentModificationException`
 * - **Weak References**: `WeakHashMap` automatically cleans up entries when owners are garbage collected
 * - **Once-Flag**: Atomic unsubscribe before handler invocation prevents double-fires
 *
 * ## Usage
 *
 * ### Method-based Subscription (Simple)
 *
 * ```kotlin
 * onMyEvent.subscribe(this) { event -> handle(event) }
 * onMyEvent.subscribe(this, once = true) { event -> handleOnce(event) }
 * onMyEvent.fire(MyEvent("data"))
 * onMyEvent.unsubscribe(this)  // optional—automatic on GC
 * ```
 *
 * ### Operator-based Subscription (Concise)
 *
 * ```kotlin
 * onMyEvent += On(this) { event -> handle(event) }          // recommended, continuous
 * onMyEvent += Once(this) { event -> handleOnce(event) }    // one-shot
 * onMyEvent += this to { event -> handle(event) }           // alternative
 * onMyEvent -= this                                         // explicit unsubscribe
 * onMyEvent(MyEvent("data"))                                // alias for fire()
 * ```
 *
 * ### Property Delegation
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
 * ## Memory Management
 *
 * Handlers are stored with owners using weak references. When the owner is garbage collected,
 * the weak reference is cleared and the handler is automatically removed. **No explicit cleanup required.**
 *
 * ```
 * Owner Instance -> [WEAK REFERENCE] -> Event Handler
 * ```
 *
 * ## Implementation Details
 *
 * - Uses `AtomicReference<WeakHashMap>` for lock-free reads and atomic writes
 * - Fire operations snapshot handlers via `.toSet()` to prevent concurrent modification
 * - Suitable for GUI event decoupling, pub/sub patterns, and concurrent environments
 *
 * **Swing EDT Integration:** Use `Event<T>.enqueue()` from `dev.robocode.tankroyale.gui.util.EDT`
 * to ensure handlers run on the Swing EDT.
 *
 * @param T the event type passed to handlers
 *
 * @see On for continuous event subscriptions (recommended)
 * @see Once for one-shot subscriptions
 * @see event for property delegation support
 */
open class Event<T> {

    // Atomic reference to WeakHashMap containing weak references to event handlers.
    // Provides lock-free reads and atomic writes. Weak references are automatically
    // cleaned up when owners are garbage collected.
    private val eventHandlers = AtomicReference(WeakHashMap<Any, Handler<T>>())

    /**
     * Subscribe to an event and provide an event handler for handling the event.
     * @param owner is the owner of the event handler, typically `this` instance.
     * @param once is a flag indication if the owner should automatically be unsubscribed when receiving an event.
     * @param priority optional priority for handler execution order (higher = earlier). Default is 0.
     * @param eventHandler is the event handler for handling the event.
     */
    fun subscribe(owner: Any, once: Boolean = false, priority: Int = 0, eventHandler: (T) -> Unit) {
        // Use atomic CAS-based operation for thread-safe insertion
        val handler = Handler(eventHandler, once, priority)
        while (true) {
            val current = eventHandlers.get()
            val updated = WeakHashMap(current)
            updated[owner] = handler
            if (eventHandlers.compareAndSet(current, updated)) {
                break
            }
        }
    }

    /**
     * Subscribe using operator syntax with [On] wrapper (recommended for continuous subscriptions).
     *
     * Example: `myEvent += On(this) { event -> println("Event: $event") }`
     * Example: `myEvent += On(this, priority = 100) { event -> println("Priority event: $event") }`
     *
     * @param subscription an [On] instance containing the owner, optional priority, and handler lambda
     * @see On for wrapper details
     */
    operator fun plusAssign(subscription: On<T>) {
        subscribe(subscription.owner, priority = subscription.priority, eventHandler = subscription.handler)
    }

    /**
     * Subscribe using operator syntax with pair notation (alternative syntax).
     *
     * Example: `myEvent += this to { event -> println("Event: $event") }`
     *
     * @param subscription a `Pair<Any, (T) -> Unit>` where first element is the owner and second is the handler lambda
     * @see Pair for the standard library Pair implementation
     */
    operator fun plusAssign(subscription: Pair<Any, (T) -> Unit>) {
        val (owner, handler) = subscription
        subscribe(owner, eventHandler = handler)
    }

    /**
     * Subscribe using operator syntax with [Once] wrapper for one-shot delivery.
     *
     * The handler will be automatically unsubscribed after it receives the first event.
     *
     * Example: `myEvent += Once(this) { event -> println("Received once: $event") }`
     * Example: `myEvent += Once(this, priority = 100) { event -> println("Priority once: $event") }`
     *
     * @param subscription an [Once] instance containing the owner, optional priority, and handler lambda
     * @see Once for wrapper details
     */
    operator fun plusAssign(subscription: Once<T>) {
        subscribe(subscription.owner, once = true, priority = subscription.priority, eventHandler = subscription.handler)
    }

    /**
     * Unsubscribes an event handler for a specific owner.
     * @param owner is the owner of the event handler, typically `this` instance.
     */
    fun unsubscribe(owner: Any) {
        while (true) {
            val current = eventHandlers.get()
            val updated = WeakHashMap(current)
            updated.remove(owner)
            if (eventHandlers.compareAndSet(current, updated)) {
                break
            }
        }
    }

    /**
     * Unsubscribe using operator syntax.
     *
     * **Note:** This is optional—handlers are automatically unsubscribed when their owners are
     * garbage collected. Explicit unsubscription is only needed for early cleanup.
     *
     * Example: `myEvent -= this`
     *
     * @param owner the owner passed during subscription (typically `this`)
     */
    operator fun minusAssign(owner: Any) {
        unsubscribe(owner)
    }

    /**
     * Fires an event to all subscribed handlers.
     * Handlers are executed in priority order (higher priority first).
     * @param event is the source event instance for the event handlers.
     */
    fun fire(event: T) {
        // Atomic lock-free read of current handlers, then snapshot via toSet() to prevent
        // ConcurrentModificationException even if handlers are modified during fire
        // Sort by priority descending (higher priority executes first)
        eventHandlers.get().entries.toSet()
            .sortedByDescending { it.value.priority }
            .forEach { entry ->
                entry.value.apply {
                    if (once) this@Event -= entry.key
                    eventHandler.invoke(event)
                }
            }
    }

    /**
     * Invoke operator as alias for [fire].
     *
     * Example: `myEvent(MyEvent("data"))`
     *
     * @param event the event instance to deliver to all subscribers
     */
    operator fun invoke(event: T) = fire(event)

    /**
     * Handler wrapper class for event handlers.
     */
    class Handler<T>(
        val eventHandler: (T) -> Unit,
        val once: Boolean = false,
        val priority: Int = 0
    )
}
