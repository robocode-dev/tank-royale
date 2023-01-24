package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.Participant
import dev.robocode.tankroyale.gui.ui.components.ConsoleFrame
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.util.Event
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel

class BotConsoleFrame(bot: Participant, frameCounter: Int = 0) :
    ConsoleFrame(bot.displayName, isTitlePropertyName = false) {

    private val onOk = Event<JButton>().apply { subscribe(this) { dispose() } }
    private val onClear = Event<JButton>().apply { subscribe(this) { clear() } }

    init {
        setLocation(10, 10 + frameCounter * 50)
        setSize(400, 300)

        val buttonPanel = JPanel().apply {
            addOkButton(onOk)
            addButton("clear", onClear)
        }

        contentPane.add(buttonPanel, BorderLayout.SOUTH)

        ClientEvents.onTickEvent.subscribe(this) { tickEvent ->
            tickEvent.botStates.filter { it.id == bot.id }[0] // TODO
        }
    }
}
