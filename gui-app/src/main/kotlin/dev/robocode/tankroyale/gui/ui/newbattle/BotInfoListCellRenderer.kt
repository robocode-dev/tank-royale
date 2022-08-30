package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.model.BotInfo
import javax.swing.JList

class BotInfoListCellRenderer : AbstractListCellRenderer() {

    override fun onRender(list: JList<out Any>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean) {
        val botInfo = value as BotInfo

        text = botInfo.displayText + " (${botInfo.host}:${botInfo.port}"

        botInfo.bootId?.let {
            botInfo.bootId?.let {
                val hex = "%X".format(it)
                text += ", $hex"
            }
        }

        text += ')'
    }
}