package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.GameStartedEvent
import dev.robocode.tankroyale.gui.model.Participant
import dev.robocode.tankroyale.gui.util.Event
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel

object SidePanel : JPanel() {

    private const val WIDTH = 120

    private val buttonsMap = HashMap<Int, JButton>()
    private val consoleMap = HashMap<Int, BotConsoleFrame>()
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
            buttonsMap[bot.id] = button

            add(button)
        }
        revalidate()
    }

    private fun onBotButtonAction(bot: Participant) {
        val id = bot.id

        var console = consoleMap[id]
        if (console == null) {
            console = BotConsoleFrame(bot, consoleMap.size)
            consoleMap[id] = console
        }
        console.isVisible = true
    }
}