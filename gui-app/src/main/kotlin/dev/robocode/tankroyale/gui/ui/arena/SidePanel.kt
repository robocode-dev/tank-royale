package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.GameStartedEvent
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel

object SidePanel : JPanel() {

    private const val WIDTH = 120

    private val buttonsMap = HashMap<Int, JButton>()

    init {
        preferredSize = Dimension(WIDTH, Int.MAX_VALUE)

        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        ClientEvents.onGameStarted.subscribe(SidePanel) { onGameStarted(it) }
    }

    private fun onGameStarted(gameStartedEvent: GameStartedEvent) {
        removeAll()

        buttonsMap.clear()

        gameStartedEvent.participants.forEach {
            val id = it.id
            val button = BotButton(id, it.name).apply {
                size = Dimension(100, height)
            }
            buttonsMap[id] = button

            add(button)
        }
        revalidate()
    }
}