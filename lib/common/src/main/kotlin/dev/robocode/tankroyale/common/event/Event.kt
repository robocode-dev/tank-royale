package dev.robocode.tankroyale.common.event

import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * A thread-safe, type-safe event system using weak references to prevent memory leaks.
 *
 * Provides C#/.NET-style delegates for Java/Kotlin with automatic handler cleanup on garbage collection.
 * Handlers are registered as weak references, so they are automatically unsubscribed when their owners are GC'd.
 *
 * ## Basic Usage
 *
 * ```kotlin
 * // Subscribe (continuous)
 * myEvent += On(this) { event -> handle(event) }
 *
 * // Subscribe (one-shot)
 * myEvent += Once(this) { event -> handleOnce(event) }
 *
 * // Fire
 * myEvent(MyEvent("data"))
 *
 * // Unsubscribe (optional—automatic on GC)
 * myEvent -= this
 * ```
 *
 * See [Subscription], [On], and [Once] for detailed usage and examples.
 *
 * ## Thread-Safety
 *
 * Fully thread-safe with atomic, lock-free semantics using `AtomicReference<WeakHashMap>`.
 * Fire operations snapshot handlers via `.toSet()` to prevent `ConcurrentModificationException`.
 * Reentrancy protection prevents infinite recursion from recursive event firing.
 *
 * ## Memory Management
 *
 * Handlers stored with weak references are automatically cleared when owners are garbage collected.
 * No explicit cleanup required.
 *
 * @param T the event type passed to handlers
 * @see Subscription for subscription type hierarchy ([On], [Once])
 */
open class Event<T> {

    // Atomic reference to WeakHashMap containing weak references to event handlers.
    // Provides lock-free reads and atomic writes. Weak references are automatically
    // cleaned up when owners are garbage collected.
    private val eventHandlers = AtomicReference(WeakHashMap<Any, Handler<T>>())

    // Per-instance, a per-thread flag to detect and prevent re-entrant event firing (infinite recursion)
    // Using object() creates a unique ThreadLocal for each Event instance
    private val isFiring = object : ThreadLocal<Boolean>() {
        override fun initialValue() = false
    }

    /**
     * Register a handler subscription.
     *
     * Thread-safe registration with atomic compare-and-swap semantics.
     * Handlers are stored with weak references to their owners for automatic cleanup on GC.
     *
     * @param owner the owner object (used as a weak reference key)
     * @param once whether this handler auto-unsubscribes after the first event (default: false for continuous)
     * @param priority handler execution order (higher = earlier). Default: 0
     * @param eventHandler the lambda to invoke on event fire
     */
    private fun subscribe(owner: Any, once: Boolean = false, priority: Int = 0, eventHandler: (T) -> Unit) {
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
     * Subscribe using operator syntax with [Subscription] wrappers.
     *
     * @param subscription a [Subscription] instance ([On] for continuous, [Once] for one-shot)
     * @see On
     * @see Once
     */
    operator fun plusAssign(subscription: Subscription<T>) {
        val once = subscription is Once<T>
        subscribe(subscription.owner, once = once, priority = subscription.priority, eventHandler = subscription.handler)
    }

    /**
     * Unregister a handler subscription.
     *
     * Thread-safe removal with atomic compare-and-swap semantics.
     *
     * @param owner the owner object passed during subscription
     */
    private fun unsubscribe(owner: Any) {
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
     * Fire an event to all subscribed handlers.
     *
     * Thread-safe with atomic, lock-free semantics. Handlers are executed in priority order
     * (higher priority first). Prevents re-entrant firing on the same thread to avoid stack overflow.
     *
     * @param event the event instance to deliver to all subscribers
     */
    private fun fire(event: T) {
        // Prevent re-entrant firing on the same thread to avoid StackOverflowError
        // This can happen if an event handler fires the same event recursively
        // CRITICAL: Check BEFORE accessing WeakHashMap to prevent overflow during .toSet()
        if (isFiring.get()) {
            return // Silently ignore re-entrant calls
        }

        isFiring.set(true)
        try {
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
        } finally {
            isFiring.set(false)
        }
    }

    /**
     * Invoke operator for firing events.
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
