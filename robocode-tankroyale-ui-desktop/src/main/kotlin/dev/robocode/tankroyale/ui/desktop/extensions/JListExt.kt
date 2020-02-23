package dev.robocode.tankroyale.ui.desktop.extensions

import javax.swing.JList
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

object JListExt {
    fun <T> JList<T>.onChanged(handler: () -> Unit) {
        model.addListDataListener(object : ListDataListener {
            override fun contentsChanged(e: ListDataEvent?) {
                handler.invoke()
            }

            override fun intervalAdded(e: ListDataEvent?) {
                handler.invoke()
            }
            override fun intervalRemoved(e: ListDataEvent?) {
                handler.invoke()
            }
        })
    }

    fun <T> JList<T>.toList(): List<T> {
        val list = ArrayList<T>()
        for (i in 0 until model.size) {
            list.add(model.getElementAt(i))
        }
        return list
    }
}