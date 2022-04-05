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
    private val eventHandlers = Collections.synchronizedMap(WeakHashMap<Any, (T) -> Unit>())

    /**
     * Subscribe to an event and provide an event handler for handling the event.
     * @param owner is the owner of the event handler, typically `this` instance.
     * @param eventHandler is the event handler for handling the event.
     */
    fun subscribe(owner: Any, eventHandler: (T) -> Unit) {
        eventHandlers[owner] = eventHandler
    }

    /**
     * Fires an event.
     * @param event is the source event instance for the event handlers.
     */
    fun fire(event: T) {
        // Work on a copy to prevent ConcurrentModificationException, even when using synchronizedMap()
        val eventHandlerCopy = ArrayList(this.eventHandlers.values)
        eventHandlerCopy.forEach { it.invoke(event) }
    }

    /**
     * Removes an event handler for a specific owner to avoid receiving and handling future events.
     * @param owner is the owner of the event handler, typically `this` instance.
     */
    fun remove(owner: Any) {
        eventHandlers.remove(owner)
    }

    /**
     * Enqueues a task on the Event Queue that must be invoked later.
     *
     * Example of usage:
     * ```
     * onEvent.enqueue { myDialog.isVisible = true }
     * ```
     * @param owner is the owner of the event handler, typically `this` instance.
     * @param runnable is used for providing the event handler function.
     */
    fun enqueue(owner: Any, runnable: () -> Unit) {
        subscribe(owner) { GuiTask.enqueue { runnable.invoke() } }
    }
}