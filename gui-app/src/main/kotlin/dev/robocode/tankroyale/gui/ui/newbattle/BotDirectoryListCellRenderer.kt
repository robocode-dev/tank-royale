package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.model.BotInfo
import javax.swing.JList

class BotDirectoryListCellRenderer : AbstractListCellRenderer() {

    override fun onRender(list: JList<out Any>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean) {
        (value as BotInfo).apply {
            text = host
        }
    }
}