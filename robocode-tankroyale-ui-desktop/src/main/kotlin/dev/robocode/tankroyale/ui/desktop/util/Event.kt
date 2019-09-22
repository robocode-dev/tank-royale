package dev.robocode.tankroyale.ui.desktop.util

import java.awt.EventQueue
import java.io.Closeable
import java.util.*

class Event<T> {

    private val subscribers = Collections.synchronizedList(ArrayList<((T) -> Unit)>())

    fun subscribe(subscriber: ((T) -> Unit)): Closeable {
        subscribers.add(subscriber)
        return disposable(subscriber)
    }

    fun publish(source: T) {
        subscribers.toList().forEach { it.invoke(source) }
    }

    fun invokeLater(runnable: (() -> Unit)): Closeable {
        return subscribe { EventQueue.invokeLater { runnable.invoke() } }
    }

    private fun disposable(subscriber: ((T) -> Unit)) = Closeable { subscribers.remove(subscriber) }
}