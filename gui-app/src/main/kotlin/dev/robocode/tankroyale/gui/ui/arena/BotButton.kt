package dev.robocode.tankroyale.gui.ui.arena

import java.awt.Dimension
import javax.swing.JButton

class BotButton(id: Int, name: String) : JButton("$id: $name") {

    override fun getMaximumSize() : Dimension {
        val size = super.getMaximumSize()
        size.width = Int.MAX_VALUE
        return size
    }
}