package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.booter.DirAndPid
import javax.swing.JList

class BootedBotCellRenderer : AbstractListCellRenderer() {

    override fun onRender(list: JList<out Any>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean) {
        (value as DirAndPid).apply { text = "$dir ($pid)" }
    }
}