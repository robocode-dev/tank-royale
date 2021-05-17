package dev.robocode.tankroyale.gui.util

import java.awt.EventQueue
import java.io.Closeable
import java.util.*

open class Event<T> {

    private val subscribers = Collections.synchronizedList(ArrayList<(T) -> Unit>())

    fun subscribe(subscriber: (T) -> Unit): Closeable {
        subscribers.add(subscriber)
        return Closeable { subscribers.remove(subscriber) }
    }

    fun publish(source: T) {
        subscribers.toList().forEach { it.invoke(source) }
    }

    fun invokeLater(runnable: () -> Unit): Closeable {
        return subscribe { EventQueue.invokeLater { runnable.invoke() } }
    }
}