package dev.robocode.tankroyale.common

import java.util.concurrent.atomic.AtomicReference
import java.util.WeakHashMap

/**
 * Used for defining an event handler. The thread handler is thread-safe and uses a WeakHashMap to get rid of event
 * handlers when their owners that are being garbage collected.
 *
 * Usage:
 * ```
 * // Declare event
 * val onMyEvent = Event<MyEvent>()
 *
 * // Subscribe to event with an event handler to handle event when it occurs
 * onMyEvent.subscribe(this) { event -> handle(event) }
 *
 * // Subscribe using operator syntax
 * onMyEvent += this to { event -> handle(event) }
 * onMyEvent += Once(this) { event -> handleOnce(event) }
 *
 * // Fire event to subscribers
 * onMyEvent.fire(MyEvent)
 *
 * // Remove event subscriber to stop receiving events
 * onMyEvent.unsubscribe(this)
 * onMyEvent -= this
 * ```
 */
open class Event<T> {

    // An atomic copy-on-write map containing weak references to event handlers. The keys are event subscribers.
    // When an owner to an event handler is being garbage collected (weak-references are always GCed),
    // the handler and its owner are automatically being removed from the map.
    private val eventHandlers = AtomicReference(WeakHashMap<Any, Handler<T>>())

    /**
     * Subscribe to an event and provide an event handler for handling the event.
     * @param owner is the owner of the event handler, typically `this` instance.
     * @param once is a flag indication if the owner should automatically be unsubscribed when receiving an event.
     * @param eventHandler is the event handler for handling the event.
     */
    fun subscribe(owner: Any, once: Boolean = false, eventHandler: (T) -> Unit) {
        eventHandlers.updateAndGet { handlers ->
            WeakHashMap(handlers).apply {
                put(owner, Handler(eventHandler, once))
            }
        }
    }

    /**
     * Subscribe using operator syntax: `event += owner to handler`.
     */
    operator fun plusAssign(subscription: Pair<Any, (T) -> Unit>) {
        val (owner, handler) = subscription
        subscribe(owner, eventHandler = handler)
    }

    /**
     * Subscribe once using operator syntax: `event += Once(owner, handler)`.
     */
    operator fun plusAssign(subscription: Once<T>) {
        subscribe(subscription.owner, once = true, eventHandler = subscription.handler)
    }

    /**
     * Unsubscribes an event handler for a specific owner to avoid receiving and handling future events.
     * @param owner is the owner of the event handler, typically `this` instance.
     */
    fun unsubscribe(owner: Any) {
        eventHandlers.updateAndGet { handlers ->
            WeakHashMap(handlers).apply {
                remove(owner)
            }
        }
    }

    /**
     * Unsubscribe using operator syntax: `event -= owner`.
     */
    operator fun minusAssign(owner: Any) {
        unsubscribe(owner)
    }

    /**
     * Fires an event.
     * @param event is the source event instance for the event handlers.
     */
    fun fire(event: T) {
        // Work on a snapshot to prevent concurrent modification issues
        eventHandlers.get().entries.forEach { (owner, handler) ->
            handler.apply {
                if (once) unsubscribe(owner)
                eventHandler.invoke(event)
            }
        }
    }

    /**
     * Invoke operator as alias for [fire].
     */
    operator fun invoke(event: T) = fire(event)

    data class HandlerData<T>(
        val eventHandler: (T) -> Unit,
        val once: Boolean
    )

    /**
     * Inline value class to avoid extra wrapper allocations for event handlers.
     */
    @JvmInline
    value class Handler<T>(private val data: HandlerData<T>) {
        constructor(eventHandler: (T) -> Unit, once: Boolean = false) : this(HandlerData(eventHandler, once))

        val eventHandler: (T) -> Unit
            get() = data.eventHandler

        val once: Boolean
            get() = data.once
    }
}

/**
 * Wrapper for subscribing with the once-flag using operator syntax.
 */
data class Once<T>(val owner: Any, val handler: (T) -> Unit)
