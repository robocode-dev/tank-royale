package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.booter.DirAndBootId
import javax.swing.JList

class RunningBotCellRenderer : AbstractListCellRenderer() {

    override fun onRender(list: JList<out Any>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean) {
        val dirAndBootId = value as DirAndBootId
        val hex = "%X".format(dirAndBootId.bootId)
        text = dirAndBootId.dir + " ($hex)"
    }
}