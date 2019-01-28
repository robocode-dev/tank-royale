package net.robocode2.gui.utils

import java.awt.EventQueue

class Observable<T> {

    private val subscribers = ArrayList<((T) -> Unit)>()

    fun subscribe(subscriber: ((T) -> Unit)) {
        subscribers.add(subscriber)
    }

    fun notifyChange(source: T) {
        subscribers.forEach {
            it.invoke(source)
        }
    }

    fun invokeLater(runnable: (() -> Unit)) {
        subscribe { EventQueue.invokeLater { runnable.invoke() } }
    }
}