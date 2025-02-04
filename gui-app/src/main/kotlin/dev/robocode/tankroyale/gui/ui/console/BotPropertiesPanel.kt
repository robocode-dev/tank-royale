package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.Participant
import dev.robocode.tankroyale.gui.model.BotPolicyUpdate
import dev.robocode.tankroyale.gui.model.TickEvent
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.SwitchButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.plaf.FontUIResource
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

class BotPropertiesPanel(val bot: Participant) : ConsolePanel() {
    private lateinit var toggleDebugGraphicsButton: SwitchButton

    override val buttonPanel: JPanel
        get() =
            JPanel().apply {
                val spacer = JLabel().apply {
                    preferredSize = Dimension(20, 1)
                }
                add(okButton)
                add(spacer)
                addLabel("toggle_graphical_debugging")
                add(createDebugGraphicsToggleButton())
            }

    private fun createDebugGraphicsToggleButton(): SwitchButton {
        toggleDebugGraphicsButton = SwitchButton(false).apply {
            addSwitchHandler { isSelected -> ClientEvents.onBotPolicyChanged.fire(BotPolicyUpdate(bot.id, isSelected)) }
        }
        return toggleDebugGraphicsButton;
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
        "graph. debugging"
    )

    private val model = DefaultTableModel(columns, properties1.count().coerceAtLeast(properties2.count()))

    init {
        setupTable()
        subscribeToEvents()

        Client.currentTick?.let {
            updateBotState(it)
        }
    }

    private fun setupTable() {
        val table = JTable(model).apply {
            setDefaultEditor(Any::class.java, null) // make it read-only
            clearSelection()
            cellSelectionEnabled = false

            columnModel.columns.iterator().forEach { column ->
                column.cellRenderer = CustomCellRenderer()
            }
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
            updateBotState(tickEvent)
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

    private fun updateBotState(tickEvent: TickEvent) {
        val botState = tickEvent.botStates.firstOrNull { it.id == bot.id } ?: return

        toggleDebugGraphicsButton.isSelected = botState.isDebuggingEnabled

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

            setValueAt(botState.enemyCount, 0, 4)
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
            setValueAt(botState.isDebuggingEnabled, 13, 4)
        }
    }

    private class CustomCellRenderer : DefaultTableCellRenderer() {
        private val monospacedFont: Font = FontUIResource(Font.MONOSPACED, Font.BOLD, 12)

        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component {
            val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            when (column) {
                1, 4 -> component.font = monospacedFont
            }
            return component
        }
    }
}