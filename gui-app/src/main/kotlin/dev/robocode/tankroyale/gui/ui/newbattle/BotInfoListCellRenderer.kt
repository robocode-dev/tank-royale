package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.model.BotInfo
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.border.EmptyBorder

class BotInfoListCellRenderer(
    private val withIcon: Boolean = false
) : JLabel(), ListCellRenderer<Any> {

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
        val pid = botInfo.pid

        text = botInfo.displayText

        if (withIcon) {
            // Rocket for boot: 🚀 (D83D DE80)
            // Global for Internet: 🌐 (D83C DF10)
            val icon = if (pid == null) "\uD83C\uDF10" else "\uD83D\uDE80"
            text = "$icon $text"
        }

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