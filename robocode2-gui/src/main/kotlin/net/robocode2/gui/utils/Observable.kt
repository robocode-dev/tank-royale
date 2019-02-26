package net.robocode2.gui.utils

import java.awt.EventQueue

class Observable<T> {

    private val subscribers = ArrayList<((T) -> Unit)>()

    fun subscribe(subscriber: ((T) -> Unit)): Disposable {
        subscribers.add(subscriber)
        return disposable(subscriber)
    }

    fun notify(source: T) {
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
                    subscribers.remove(subscriber)
                    disposed = true
                }
            }
}