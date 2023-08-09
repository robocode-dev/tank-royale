package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.GameStartedEvent
import dev.robocode.tankroyale.gui.model.Participant
import dev.robocode.tankroyale.gui.ui.console.BotConsoleFrame
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.gui.util.Event
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel

typealias BotIdentifier = String

object SidePanel : JPanel() {

    private const val WIDTH = 160

    private val buttonsMap = HashMap<BotIdentifier, JButton>()
    private val consoleMap = HashMap<BotIdentifier, BotConsoleFrame>()
    private val buttonsEvent = Event<BotButton>() // shared between all buttons

    init {
        preferredSize = Dimension(WIDTH, Int.MAX_VALUE)

        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        ClientEvents.onGameStarted.subscribe(SidePanel) { onGameStarted(it) }

        buttonsEvent.subscribe(SidePanel) { onBotButtonAction(it.bot) }
    }

    private fun onGameStarted(gameStartedEvent: GameStartedEvent) {
        removeAll()

        buttonsMap.clear()

        gameStartedEvent.participants.forEach { bot ->
            val button = BotButton(bot).apply {
                addActionListener { buttonsEvent.fire(this) }
            }
            buttonsMap[bot.displayName] = button

            add(button)

            revalidate()
        }
    }

    private fun onBotButtonAction(bot: Participant) {
        var console = consoleMap[bot.displayName]
        if (console == null) {
            console = BotConsoleFrame(bot, consoleMap.size)
            consoleMap[bot.displayName] = console

            console.onClosing {
                consoleMap.remove(bot.displayName)
            }
        }
        console.isVisible = true
    }
}