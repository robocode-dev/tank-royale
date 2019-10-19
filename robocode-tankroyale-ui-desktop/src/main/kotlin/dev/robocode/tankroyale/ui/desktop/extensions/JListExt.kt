package dev.robocode.tankroyale.ui.desktop.extensions

import javax.swing.JList
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

object JListExt {
    fun <T> JList<T>.onContentsChanged(handler: () -> Unit) {
        this.model.addListDataListener(object: ListDataListener {
            override fun contentsChanged(e: ListDataEvent?) {
                handler.invoke()
            }
            override fun intervalAdded(e: ListDataEvent?) {}
            override fun intervalRemoved(e: ListDataEvent?) {}
        })
    }
}