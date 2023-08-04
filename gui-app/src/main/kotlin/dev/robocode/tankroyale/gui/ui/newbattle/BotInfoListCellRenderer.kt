package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.model.BotInfo
import javax.swing.JList

class BotInfoListCellRenderer : AbstractListCellRenderer() {

    override fun onRender(list: JList<out Any>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean) {
        (value as BotInfo).apply {
            text = "$displayText (${host(host)}:$port)" + (teamName?.let { " / $teamName $teamVersion" } ?: "")
        }
    }

    private fun host(hostName: String) =
        if (hostName == "0:0:0:0:0:0:0:1" || hostName == "127.0.0.1") {
            "localhost"
        } else {
            hostName
        }
}