package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.model.Participant
import dev.robocode.tankroyale.gui.ui.Strings
import javax.swing.JTabbedPane

class BotConsoleFrame(val bot: Participant, frameCounter: Int = 0) :
    ConsoleFrame(bot.displayName, isTitlePropertyName = false, consolePanel = BotConsolePanel(bot)) {

    private val propertiesPanel = BotPropertiesPanel(bot)
    private val eventsPanel = BotEventsPanel(bot)

    init {
        setLocation(10, 10 + frameCounter * 50) // increment y for each bot console frame
        setSize(600, 400)

        add(JTabbedPane().apply {
            addTab(Strings.get("bot_console.console"), consolePanel)
            addTab(Strings.get("bot_console.properties"), propertiesPanel)
            addTab(Strings.get("bot_console.events"), eventsPanel) // The rendering is currently too slow!
        })
    }
}
