package dev.robocode.tankroyale.gui.ui.components

import dev.robocode.tankroyale.client.model.BotInfo
import java.awt.EventQueue
import java.util.*
import javax.swing.AbstractListModel

class SortedListModel<T : Comparable<T>> : AbstractListModel<T>() {

    private val list = ArrayList<T>()

    override fun getSize(): Int = list.size

    override fun getElementAt(index: Int): T = list[index]

    fun addElement(element: T) {
        onEdt {
            list.add(element)
            list.sortWith { o1, o2 -> compare(element, o1, o2) }
            val index = list.indexOf(element)
            fireIntervalAdded(this, index, index)
        }
    }

    fun clear() {
        onEdt {
            val size = list.size
            if (size > 0) {
                list.clear()
                fireIntervalRemoved(this, 0, size - 1)
            }
        }
    }

    operator fun contains(element: T): Boolean = list.contains(element)

    fun removeElement(element: T): Boolean {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater { removeElement(element) }
            return false
        }
        val index = list.indexOf(element)
        if (index < 0) return false
        list.removeAt(index)
        fireIntervalRemoved(this, index, index)
        return true
    }

    operator fun get(index: Int): T = list[index]

    fun list(): List<T> = Collections.unmodifiableList(list)

    private fun onEdt(block: () -> Unit) {
        if (EventQueue.isDispatchThread()) block() else EventQueue.invokeLater(block)
    }

    private fun compare(element: T, o1: T, o2: T): Int =
        when (element) {
            is BotInfo -> {
                val b1 = o1 as BotInfo
                val b2 = o2 as BotInfo
                b1.host.lowercase(Locale.getDefault()).compareTo(b2.host.lowercase(Locale.getDefault()))
            }
            else -> o1.toString().lowercase(Locale.getDefault())
                .compareTo(o2.toString().lowercase(Locale.getDefault()))
        }
}
