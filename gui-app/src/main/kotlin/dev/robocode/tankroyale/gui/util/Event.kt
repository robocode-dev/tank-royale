package dev.robocode.tankroyale.gui.util

import java.util.*

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
 * // Fire event to subscribers
 * onMyEvent.fire(MyEvent)
 *
 * // Remove event subscriber to stop receiving events
 * onMyEvent.remove(this)
 * ```
 */
open class Event<T> {

    // A synchronized map containing weak references to event handlers. The keys are event subscribers.
    // When an owner to an event handler is being garbage collected (weak-references are always GCed),
    // the handler and its owner are automatically being removed from the map.
    private val eventHandlers = Collections.synchronizedMap(WeakHashMap<Any, Handler<T>>())

    /**
     * Subscribe to an event and provide an event handler for handling the event.
     * @param owner is the owner of the event handler, typically `this` instance.
     * @param once is a flag indication if the owner should automatically be unsubscribed when receiving an event.
     * @param eventHandler is the event handler for handling the event.
     */
    fun subscribe(owner: Any, once: Boolean = false, eventHandler: (T) -> Unit) {
        eventHandlers[owner] = Handler(eventHandler, once)
    }

    /**
     * Unsubscribes an event handler for a specific owner to avoid receiving and handling future events.
     * @param owner is the owner of the event handler, typically `this` instance.
     */
    fun unsubscribe(owner: Any) {
        eventHandlers.remove(owner)
    }

    /**
     * Fires an event.
     * @param event is the source event instance for the event handlers.
     */
    fun fire(event: T) {
        // Work on a copy to prevent ConcurrentModificationException, even when using synchronizedMap()
        eventHandlers.entries.toSet().forEach { (owner, handler) ->
            handler.apply {
                if (once) unsubscribe(owner)
                eventHandler.invoke(event)
            }
        }
    }

    /**
     * Enqueues a task on the Event Queue that must be invoked later.
     *
     * Example of usage:
     * ```
     * onEvent.enqueue { myDialog.isVisible = true }
     * ```
     * @param owner is the owner of the event handler, typically `this` instance.
     * @param callable is used for providing the event handler function.
     */
    fun enqueue(owner: Any, callable: () -> Unit) {
        subscribe(owner) { EDT.enqueue { callable.invoke() } }
    }

    class Handler<T>(
        val eventHandler: (T) -> Unit,
        val once: Boolean = false
    )
}