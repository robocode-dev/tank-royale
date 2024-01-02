package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.ui.components.RcList
import dev.robocode.tankroyale.gui.ui.components.SortedListModel
import dev.robocode.tankroyale.gui.util.Event
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

class BotList<T : Comparable<T>>(sortedListModel: SortedListModel<T>, readOnly: Boolean = true) : RcList<T>(sortedListModel) {

    val onDeleteKeyTyped = Event<List<T>>() // List of all selected elements

    init {
        if (!readOnly)
            onDeleteKeyRemoveSelectedEntry(sortedListModel)
    }

    private fun onDeleteKeyRemoveSelectedEntry(sortedListModel: SortedListModel<T>) {
        addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent?) {
                if (e?.keyChar == KeyEvent.VK_DELETE.toChar() && selectedIndices.isNotEmpty()) {
                    val elements = ArrayList<T>()
                    selectedIndices.reversed().forEach { selectedIndex ->
                        val element: T = model.getElementAt(selectedIndex)
                        elements += element
                        sortedListModel.removeElement(element)
                    }
                    revalidate()
                    repaint()
                    onDeleteKeyTyped.fire(elements)
                }
            }
        })
    }
}