package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.model.Participant
import java.awt.Dimension
import javax.swing.JButton

class BotButton(val bot: Participant) : JButton(bot.displayName) {

    override fun getMaximumSize() : Dimension {
        val size = super.getMaximumSize()
        size.width = Int.MAX_VALUE
        return size
    }
}