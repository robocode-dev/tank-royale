package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.booter.DirAndPid
import dev.robocode.tankroyale.gui.model.BotInfo
import javax.swing.JList

class RunningBotCellRenderer : AbstractListCellRenderer() {

    override fun onRender(list: JList<out Any>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean) {
        val dirAndPid = value as DirAndPid
        text = dirAndPid.dir + "(" + dirAndPid.pid + ")"
    }
}