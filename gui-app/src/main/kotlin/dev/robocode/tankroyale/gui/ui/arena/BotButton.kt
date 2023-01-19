package dev.robocode.tankroyale.gui.ui.arena

import java.awt.Dimension
import javax.swing.JButton

class BotButton(botId: Int, botName: String) : JButton("$botId: $botName") {

    override fun getMaximumSize() : Dimension {
        val size = super.getMaximumSize()
        size.width = Int.MAX_VALUE
        return size
    }
}