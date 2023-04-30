package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.BotState
import dev.robocode.tankroyale.gui.model.Participant
import dev.robocode.tankroyale.gui.model.TickEvent
import dev.robocode.tankroyale.gui.ui.Strings
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class BotPropertiesPanel(val bot: Participant) : ConsolePanel() {

    override val buttonPanel
        get() = JPanel().apply {
            add(okButton)
        }

    private val columns = arrayOf(
        Strings.get("bot_console.properties.property"),
        Strings.get("bot_console.properties.value"),
        "",
        Strings.get("bot_console.properties.property"),
        Strings.get("bot_console.properties.value")
    )

    private val properties1 = listOf(
        "session id",
        "id",
        "round",
        "turn",
        "energy",
        "x",
        "y",
        "speed",
        "direction",
        "gun direction",
        "radar direction",
        "radar sweep",
        "gun heat",
    )

    private val properties2 = listOf(
        "enemy count",
        "body color",
        "turret color",
        "radar color",
        "bullet color",
        "scan color",
        "tracks color",
        "gun color",
        "turn rate",
        "gun turn rate",
        "radar turn rate",
        "standard output",
        "standard error",
    )

    private val model = DefaultTableModel(columns, properties1.count().coerceAtLeast(properties2.count()))

    init {
        setupTable()
        subscribeToEvents()
    }

    private fun setupTable() {
        val table = JTable(model).apply {
            setDefaultEditor(Any::class.java, null) // make it read-only
            clearSelection()
            cellSelectionEnabled = false
        }

        layout = BorderLayout()
        add(table.tableHeader, BorderLayout.PAGE_START)
        add(table, BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.SOUTH)

        table.columnModel.getColumn(0).apply {
            minWidth = 120
            maxWidth = 120
        }
        table.columnModel.getColumn(2).apply {
            minWidth = 15
            maxWidth = 15
        }
        table.columnModel.getColumn(3).apply {
            minWidth = 120
            maxWidth = 120
        }

        var row = 0
        properties1.forEach {
            model.setValueAt(it, row++, 0)
        }
        row = 0
        properties2.forEach {
            model.setValueAt(it, row++, 3)
        }
    }

    private fun subscribeToEvents() {
        ClientEvents.onTickEvent.subscribe(this) { tickEvent ->
            val botStates = tickEvent.botStates.filter { it.id == bot.id }
            if (botStates.isNotEmpty()) {
                updateBotState(botStates[0], tickEvent)
            }
        }
        ClientEvents.onGameStarted.subscribe(this) {
            subscribeToEvents()
        }
        ClientEvents.onGameEnded.subscribe(this) {
            unsubscribeEvents()
        }
        ClientEvents.onGameAborted.subscribe(this) {
            unsubscribeEvents()
        }
    }

    private fun unsubscribeEvents() {
        ClientEvents.onTickEvent.unsubscribe(this)
    }

    private fun updateBotState(botState: BotState, tickEvent: TickEvent) {
        model.apply {
            // Column 1

            setValueAt(botState.sessionId, 0, 1)
            setValueAt(botState.id, 1, 1)
            setValueAt(tickEvent.roundNumber, 2, 1)
            setValueAt(tickEvent.turnNumber, 3, 1)
            setValueAt(botState.energy, 4, 1)
            setValueAt(botState.x, 5, 1)
            setValueAt(botState.y, 6, 1)
            setValueAt(botState.speed, 7, 1)
            setValueAt(botState.direction, 8, 1)
            setValueAt(botState.gunDirection, 9, 1)
            setValueAt(botState.radarDirection, 10, 1)
            setValueAt(botState.radarSweep, 11, 1)
            setValueAt(botState.gunHeat, 12, 1)

            // Column 2

            setValueAt(tickEvent.botStates.count(), 0, 4)
            setValueAt(botState.bodyColor, 1, 4)
            setValueAt(botState.turretColor, 2, 4)
            setValueAt(botState.radarColor, 3, 4)
            setValueAt(botState.bulletColor, 4, 4)
            setValueAt(botState.scanColor, 5, 4)
            setValueAt(botState.tracksColor, 6, 4)
            setValueAt(botState.gunColor, 7, 4)
            setValueAt(botState.turnRate, 8, 4)
            setValueAt(botState.gunTurnRate, 9, 4)
            setValueAt(botState.radarTurnRate, 10, 4)
            setValueAt(botState.stdOut, 11, 4)
            setValueAt(botState.stdErr, 12, 4)
        }
    }
}