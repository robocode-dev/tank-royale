package net.robocode2.gui.utils

import sun.misc.Cleaner
import java.awt.EventQueue

class Event<T> {

    private val subscribers = ArrayList<((T) -> Unit)>()

    fun subscribe(subscriber: ((T) -> Unit)): Disposable {
        subscribers.add(subscriber)
        Cleaner.create(subscriber) {
            subscribers.remove(subscriber)
        }
        return disposable(subscriber)
    }

    fun publish(source: T) {
        subscribers.forEach {
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