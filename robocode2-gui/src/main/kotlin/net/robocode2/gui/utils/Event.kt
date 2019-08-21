package net.robocode2.gui.utils

import java.awt.EventQueue
import java.util.*

class Event<T> {

    private val subscribers = Collections.synchronizedList(ArrayList<((T) -> Unit)>())

    fun subscribe(subscriber: ((T) -> Unit)): Disposable {
        subscribers.add(subscriber)
        return disposable(subscriber)
    }

    fun publish(source: T) {
        subscribers.toList().forEach {
            it.invoke(source)
        }
    }

    fun invokeLater(runnable: (() -> Unit)): Disposable {
        return subscribe { EventQueue.invokeLater { runnable.invoke() } }
    }

    private fun disposable(subscriber: ((T) -> Unit)) =
        object : Disposable {
            private var disposed = false

            override val isDisposed: Boolean
                get() = disposed

            override fun dispose() {
                if (!disposed) {
                    subscribers.remove(subscriber)
                    disposed = true
                }
            }
        }
}