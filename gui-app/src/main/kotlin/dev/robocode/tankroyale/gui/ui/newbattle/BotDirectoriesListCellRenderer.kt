package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.model.BotInfo
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.border.EmptyBorder

class BotDirectoriesListCellRenderer : JLabel(), ListCellRenderer<Any> {
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

        val botInfo = value as BotInfo
        text = botInfo.host

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
        return this
    }
}