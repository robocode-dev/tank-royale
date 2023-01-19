package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.ui.components.ConsoleFrame
import java.awt.BorderLayout
import javax.swing.JButton

class BotConsoleFrame(botId: Int, botName: String) : ConsoleFrame("$botId: $botName", isTitlePropertyName = false) {

    init {
        setSize(400, 300)

        contentPane.add(JButton("Hest"), BorderLayout.SOUTH)
    }
}

fun main() {
    BotConsoleFrame(1, "test").isVisible = true
}