package dev.robocode.tankroyale.gui.ui.components

import dev.robocode.tankroyale.gui.util.GuiTask.enqueue
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.AbstractListModel

class SortedListModel<T : Comparable<T>> : AbstractListModel<T>() {
    private val list = CopyOnWriteArrayList<T>()

    override fun getSize(): Int {
        return list.size
    }

    override fun getElementAt(index: Int): T {
        return list[index]
    }

    fun addElement(element: T) {
        list.add(element)
        list.sort()
        notifyChanged()
    }

    fun clear() {
        list.clear()
        notifyChanged()
    }

    operator fun contains(element: T): Boolean {
        return list.contains(element)
    }

    fun removeElement(element: T): Boolean {
        val removed = list.remove(element)
        if (removed) {
            notifyChanged()
        }
        return removed
    }

    operator fun get(index: Int): T {
        return getElementAt(index)
    }

    fun list(): List<T> {
        return Collections.unmodifiableList(list)
    }

    private fun notifyChanged() {
        enqueue { // if omitted, the JList might not update correctly?!
            fireContentsChanged(this, 0, size)
        }
    }
}