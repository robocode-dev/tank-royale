package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.model.BotInfo
import javax.swing.JList

class BotInfoListCellRenderer : AbstractListCellRenderer() {

    override fun onRender(list: JList<out Any>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean) {
        val botInfo = value as BotInfo
        val pid = botInfo.pid

        text = botInfo.displayText

        // Rocket for boot: 🚀 (D83D DE80)
        // Global for Internet: 🌐 (D83C DF10)
        val icon = if (pid == null) "\uD83C\uDF10" else "\uD83D\uDE80"
        text = "$icon $text"
    }
}