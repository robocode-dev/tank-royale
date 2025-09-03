package dev.robocode.tankroyale.gui.ui.extensions

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
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

    fun <T> JList<T>.onSelection(handler: (T) -> Unit) {
        addListSelectionListener {
            val index = selectedIndex
            if (index >= 0) {
                handler.invoke(model.getElementAt(index))
            }
        }
    }

    fun <T> JList<T>.onMultiClickedAtIndex(handler: (Int) -> Unit) {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    handler.invoke(locationToIndex(e.point))
                }
            }
        })
    }
}