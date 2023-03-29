package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.model.Participant

class BotConsoleFrame(val bot: Participant, frameCounter: Int = 0) :
    ConsoleFrame(bot.displayName, isTitlePropertyName = false, consolePanel = BotConsolePanel(bot)) {

    init {
        setLocation(10, 10 + frameCounter * 50) // increment y for each bot console frame
        setSize(600, 400)
    }
}
