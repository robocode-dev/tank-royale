package dev.robocode.tankroyale.gui.ui.components

import dev.robocode.tankroyale.gui.model.BotInfo
import java.awt.EventQueue
import java.util.*
import javax.swing.AbstractListModel

class SortedListModel<T : Comparable<T>> : AbstractListModel<T>() {

    private val list = Collections.synchronizedList(ArrayList<T>())

    override fun getSize(): Int {
        return list.size
    }

    override fun getElementAt(index: Int): T {
        return list[index]
    }

    fun addElement(element: T) {
        list.add(element)

        list.sortWith { o1, o2 ->
            when (element) {
                is BotInfo -> {
                    val b1 = o1 as BotInfo
                    val b2 = o2 as BotInfo
                    b1.host.lowercase(Locale.getDefault()).compareTo(b2.host.lowercase(Locale.getDefault()))
                }

                else -> {
                    o1.toString().lowercase(Locale.getDefault())
                        .compareTo(o2.toString().lowercase(Locale.getDefault()))
                }
            }
        }
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
        EventQueue.invokeLater { // if omitted, the JList might not update correctly?!
            EventQueue.invokeLater { // if omitted, the JList might not update correctly?!
                fireContentsChanged(this, 0, size)
            }
        }
    }
}