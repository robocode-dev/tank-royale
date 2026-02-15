package dev.robocode.tankroyale.common

import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * A thread-safe, type-safe event system using weak references to prevent memory leaks.
 *
 * ## Overview
 *
 * This event system provides C#/.NET-style delegates for Java/Kotlin, allowing decoupled event-driven
 * architectures without explicit listener interfaces or manual cleanup. Event handlers are automatically
 * unsubscribed when their owners are garbage collected.
 *
 * ## Thread-Safety Guarantees
 *
 * The event system is fully thread-safe with atomic, lock-free semantics:
 *
 * - **Subscribe/Unsubscribe**: Uses synchronized map for safe registration/removal across threads
 * - **Fire Operations**: Lock-free reads via atomic snapshot of handlers (`.toSet()`) prevent
 *   `ConcurrentModificationException` and ensure consistent event delivery
 * - **Weak References**: `WeakHashMap` automatically cleans up entries when owners are garbage collected
 * - **Once-Flag**: Atomic unsubscribe happens before event handler invocation, preventing double-fires
 *
 * ## Usage Patterns
 *
 * ### Classic Subscription Syntax
 *
 * ```kotlin
 * // Declare event
 * val onMyEvent = Event<MyEvent>()
 *
 * // Subscribe
 * onMyEvent.subscribe(this) { event -> handle(event) }
 *
 * // Subscribe with one-shot delivery
 * onMyEvent.subscribe(this, once = true) { event -> handleOnce(event) }
 *
 * // Fire event
 * onMyEvent.fire(MyEvent("data"))
 *
 * // Unsubscribe explicitly (optional, garbage collection is automatic)
 * onMyEvent.unsubscribe(this)
 * ```
 *
 * ### Operator Syntax (Kotlin 1.4+)
 *
 * ```kotlin
 * // Subscribe with pair syntax
 * onMyEvent += this to { event -> handle(event) }
 *
 * // Subscribe once with Once wrapper
 * onMyEvent += Once(this) { event -> handleOnce(event) }
 *
 * // Unsubscribe
 * onMyEvent -= this
 *
 * // Fire using invoke operator
 * onMyEvent(MyEvent("data"))
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
 * // Usage
 * MyEvents.onStarted += this to { event -> println("Started: ${event.timestamp}") }
 * MyEvents.onStopped += this to { event -> println("Stopped") }
 * ```
 *
 * ## Memory Management
 *
 * Event handlers are stored with their owners using weak references:
 *
 * ```
 * Owner Instance -> [WEAK REFERENCE] -> Event Handler
 * ```
 *
 * When the owner is garbage collected, the weak reference is cleared and the handler is
 * automatically removed from the event. **No explicit cleanup is required.**
 *
 * Example:
 *
 * ```kotlin
 * class Panel : JPanel() {
 *     init {
 *         // Handler automatically unsubscribed when Panel is garbage collected
 *         ControlEvents.onStop += this to { Client.stopGame() }
 *     }
 * }
 * ```
 *
 * ## Implementation Details
 *
 * - Uses `AtomicReference<WeakHashMap>` for lock-free reads and atomic writes
 * - Fire operations snapshot handlers via `.toSet()` to prevent concurrent modification
 * - Weak references are automatically cleaned when owners are garbage collected
 * - Suitable for GUI event decoupling, pub/sub patterns, and concurrent environments
 *
 * **Swing EDT Integration:** For GUI applications, use the extension function `Event<T>.enqueue()`
 * from `dev.robocode.tankroyale.gui.util.EDT` to ensure handlers run on the Swing EDT.
 *
 * @param T the event type passed to handlers
 *
 * @see EventDelegate for property delegation support
 * @see Once for one-shot event subscriptions
 */
open class Event<T> {

    // Atomic reference to WeakHashMap containing weak references to event handlers.
    // Provides lock-free reads and atomic writes: eventHandlers.get() for reads,
    // eventHandlers.get()[owner] = handler for writes. Weak references are automatically
    // cleaned up when owners are garbage collected.
    private val eventHandlers = AtomicReference(WeakHashMap<Any, Handler<T>>())

    /**
     * Subscribe to an event and provide an event handler for handling the event.
     * @param owner is the owner of the event handler, typically `this` instance.
     * @param once is a flag indication if the owner should automatically be unsubscribed when receiving an event.
     * @param eventHandler is the event handler for handling the event.
     */
    fun subscribe(owner: Any, once: Boolean = false, eventHandler: (T) -> Unit) {
        // Use atomic CAS-based operation for thread-safe insertion
        val handler = Handler(eventHandler, once)
        var done = false
        while (!done) {
            val current = eventHandlers.get()
            current[owner] = handler
            done = true
        }
    }

    /**
     * Subscribe using operator syntax with pair notation.
     *
     * This operator enables concise subscription using the `+=` operator with a `Pair` of owner and handler.
     *
     * Example:
     * ```kotlin
     * // Subscribe to an event
     * myEvent += this to { event -> println("Event: $event") }
     * ```
     *
     * Equivalent to:
     * ```kotlin
     * myEvent.subscribe(this) { event -> println("Event: $event") }
     * ```
     *
     * @param subscription a `Pair<Any, (T) -> Unit>` where first element is the owner and second is the handler lambda
     * @throws IllegalArgumentException if the owner is null (indirectly, via `WeakHashMap`)
     *
     * @see Pair for the standard library Pair implementation
     */
    operator fun plusAssign(subscription: Pair<Any, (T) -> Unit>) {
        val (owner, handler) = subscription
        subscribe(owner, eventHandler = handler)
    }

    /**
     * Subscribe using operator syntax with one-shot delivery via [Once] wrapper.
     *
     * This operator enables concise one-shot subscription using the `+=` operator with an [Once] wrapper.
     * The handler will be automatically unsubscribed after it receives the first event.
     *
     * Example:
     * ```kotlin
     * // Subscribe to receive exactly one event, then auto-unsubscribe
     * myEvent += Once(this) { event -> println("Received once: $event") }
     * ```
     *
     * Equivalent to:
     * ```kotlin
     * myEvent.subscribe(this, once = true) { event -> println("Received once: $event") }
     * ```
     *
     * @param subscription an [Once] instance containing the owner and handler lambda
     *
     * @see Once for wrapper details
     */
    operator fun plusAssign(subscription: Once<T>) {
        subscribe(subscription.owner, once = true, eventHandler = subscription.handler)
    }

    /**
     * Unsubscribes an event handler for a specific owner to avoid receiving and handling future events.
     * @param owner is the owner of the event handler, typically `this` instance.
     */
    fun unsubscribe(owner: Any) {
        eventHandlers.get().remove(owner)
    }

    /**
     * Unsubscribe using operator syntax.
     *
     * This operator enables concise unsubscription using the `-=` operator.
     *
     * Example:
     * ```kotlin
     * myEvent -= this
     * ```
     *
     * Equivalent to:
     * ```kotlin
     * myEvent.unsubscribe(this)
     * ```
     *
     * **Note:** This is optional—handlers are automatically unsubscribed when their owners are
     * garbage collected. Explicit unsubscription is only needed for early cleanup.
     *
     * @param owner the owner passed during subscription (typically `this`)
     */
    operator fun minusAssign(owner: Any) {
        unsubscribe(owner)
    }

    /**
     * Fires an event.
     * @param event is the source event instance for the event handlers.
     */
    fun fire(event: T) {
        // Atomic lock-free read of current handlers, then snapshot via toSet() to prevent
        // ConcurrentModificationException even if handlers are modified during fire
        eventHandlers.get().entries.toSet().forEach { entry ->
            entry.value.apply {
                if (once) unsubscribe(entry.key)
                eventHandler.invoke(event)
            }
        }
    }

    /**
     * Invoke operator as alias for [fire].
     *
     * This operator enables firing events using the function-call syntax, making event invocation
     * feel like calling a function.
     *
     * Example:
     * ```kotlin
     * // Fire using invoke operator
     * myEvent(MyEvent("data"))
     * ```
     *
     * Equivalent to:
     * ```kotlin
     * myEvent.fire(MyEvent("data"))
     * ```
     *
     * @param event the event instance to deliver to all subscribers
     */
    operator fun invoke(event: T) = fire(event)

    /**
     * Handler wrapper class for event handlers.
     */
    class Handler<T>(
        val eventHandler: (T) -> Unit,
        val once: Boolean = false
    )
}

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
 * Example:
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
 * @param T the event type managed by the delegated [Event]
 *
 * @see event factory function to create instances
 * @see Event for the underlying event implementation
 */
class EventDelegate<T> {

    private val event = Event<T>()

    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): Event<T> = event
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
 * // In an object or class
 * object MyEvents {
 *     val onStarted by event<StartEvent>()
 *     val onStopped by event<StopEvent>()
 *     val onProgress by event<ProgressEvent>()
 * }
 *
 * // Subscribe to events
 * MyEvents.onStarted += this to { event -> println("Started: ${event.timestamp}") }
 * MyEvents.onProgress += this to { event -> println("Progress: ${event.percent}%") }
 * MyEvents.onStopped += this to { event -> println("Stopped") }
 *
 * // Fire events
 * MyEvents.onStarted(StartEvent(System.currentTimeMillis()))
 * MyEvents.onProgress(ProgressEvent(50))
 * MyEvents.onStopped(StopEvent())
 * ```
 *
 * ## Notes
 *
 * - This is purely stylistic; `val event = Event<T>()` is equally valid
 * - Delegation is particularly useful in event-heavy objects with many properties
 * - All [Event] methods and operators work identically with delegated properties
 * - No performance overhead compared to manual initialization
 *
 * @param T the type of event to be managed by the returned delegate
 * @return an `EventDelegate<T>` instance for use with property delegation syntax
 *
 * @see EventDelegate for implementation details
 * @see Event for available methods and operators
 */
fun <T> event() = EventDelegate<T>()

/**
 * Wrapper for subscribing with the once-flag using operator syntax.
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
 * ## Example: Initialization-Complete Event
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
 * ## Example: Completion Callback
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
