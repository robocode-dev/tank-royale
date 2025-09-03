package dev.robocode.tankroyale.gui.ui.newbattle

import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.border.EmptyBorder

abstract class AbstractListCellRenderer : JLabel(), ListCellRenderer<Any> {
    init {
        isOpaque = true
    }

    override fun getListCellRendererComponent(
        list: JList<out Any>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        border = EmptyBorder(1, 1, 1, 1)

        if (list != null) {
            if (isSelected) {
                background = list.selectionBackground
                foreground = list.selectionForeground
            } else {
                background = list.background
                foreground = list.foreground
            }
            font = list.font
        }
        onRender(list, value, index, isSelected, cellHasFocus)

        return this
    }

    abstract fun onRender(list: JList<out Any>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean)
}