package dev.robocode.tankroyale.gui.ui.components

import javax.swing.AbstractListModel
import java.util.SortedSet
import java.util.TreeSet

class SortedListModel : AbstractListModel<Any>() {
    private val model: SortedSet<Any> = TreeSet()

    override fun getSize(): Int {
        return model.size
    }

    override fun getElementAt(index: Int): Any {
        return model.toTypedArray()[index]
    }

    fun addElement(element: Any) {
        if (model.add(element)) {
            fireContentsChanged(this, 0, size)
        }
    }

    fun clear() {
        model.clear()
        fireContentsChanged(this, 0, size)
    }

    operator fun contains(element: Any): Boolean {
        return model.contains(element)
    }

    fun removeElement(element: Any): Boolean {
        val removed = model.remove(element)
        if (removed) {
            fireContentsChanged(this, 0, size)
        }
        return removed
    }

    operator fun get(index: Int): Any {
        return getElementAt(index)
    }

    fun toArray(): Array<Any> {
        return model.toTypedArray()
    }
}