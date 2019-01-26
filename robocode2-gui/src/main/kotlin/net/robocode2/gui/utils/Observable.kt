package net.robocode2.gui.utils

import java.awt.EventQueue

class Observable {

    private val subscribers = ArrayList<(() -> Unit)>()

    fun subscribe(subscriber: (() -> Unit)) {
        subscribers.add(subscriber)
    }

    fun notifyChange() {
        subscribers.forEach {
            it.invoke()
        }
    }

    fun invokeLater(runnable: ((Unit) -> Unit)) {
        subscribe { EventQueue.invokeLater { runnable.invoke(Unit) } }
    }
}