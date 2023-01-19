package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.ui.components.ConsoleFrame
import java.awt.BorderLayout
import javax.swing.JPanel

class BotConsoleFrame(botId: Int, botName: String) : ConsoleFrame("$botId: $botName", isTitlePropertyName = false) {

    init {
        setSize(400, 300)

        val buttonPanel = JPanel().apply {


        }

        contentPane.add(buttonPanel, BorderLayout.SOUTH)
    }
}

fun main() {
    BotConsoleFrame(1, "test").isVisible = true
}