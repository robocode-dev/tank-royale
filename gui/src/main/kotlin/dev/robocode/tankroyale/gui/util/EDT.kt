package dev.robocode.tankroyale.gui.util

import dev.robocode.tankroyale.common.Event
import dev.robocode.tankroyale.gui.ui.BusyCursor
import java.awt.EventQueue

object EDT {

    fun enqueue(callable: () -> Unit) {
        EventQueue.invokeLater {
            BusyCursor.activate()
            try {
                callable.invoke()
            } finally {
                BusyCursor.deactivate()
            }
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
fun <T> Event<T>.enqueue(owner: Any, callable: () -> Unit) {
    this.subscribe(owner) { EDT.enqueue { callable.invoke() } }
}
