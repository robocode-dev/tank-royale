package dev.robocode.tankroyale.gui.ui.selection

import dev.robocode.tankroyale.gui.model.BotInfo
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.border.EmptyBorder

class BotInfoListCellRenderer : JLabel(), ListCellRenderer<BotInfo> {

    init {
        isOpaque = true
    }

    override fun getListCellRendererComponent(
        list: JList<out BotInfo>, value: BotInfo, index: Int, isSelected: Boolean, cellHasFocus: Boolean
    ): Component {

        text = value.displayText
        border = EmptyBorder(1, 1, 1, 1)

        if (isSelected) {
            background = list.selectionBackground
            foreground = list.selectionForeground
        } else {
            background = list.background
            foreground = list.foreground
        }
        font = list.font

        return this
    }
}