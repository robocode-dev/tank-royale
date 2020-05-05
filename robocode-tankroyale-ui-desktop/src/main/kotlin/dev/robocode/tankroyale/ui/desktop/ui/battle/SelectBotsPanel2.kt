package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.table.DefaultTableModel


class SelectBotsPanel2 : JPanel(MigLayout("fill")) {
    // Private events
    private val onAdd = Event<JButton>()
    private val onAddAll = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onRemoveAll = Event<JButton>()

    val availableBotTable = BotSelectionTable()
    val selectedBotTable = BotSelectionTable()

    init {
        val leftSelectionPanel = JPanel(MigLayout("fill")).apply {
            val scrollPane = JScrollPane(availableBotTable)
            add(scrollPane, "grow")
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("available_bots"))
        }

        val rightSelectionPanel = JPanel(MigLayout("fill")).apply {
            add(JScrollPane(selectedBotTable), "grow")
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("selected_bots"))
        }

        val addPanel = JPanel(MigLayout("fill", "[fill]"))
        val removePanel = JPanel(MigLayout("fill", "[fill]"))

        val middlePanel = JPanel(MigLayout("fill"))

        val centerSelectionPanel = JPanel(MigLayout()).apply {
            add(addPanel, "north")
            add(middlePanel, "h 300")
            add(removePanel, "south")
        }
        val selectionPanel = JPanel(MigLayout("", "[grow][][grow]")).apply {
            add(leftSelectionPanel, "grow")
            add(centerSelectionPanel, "")
            add(rightSelectionPanel, "grow")
        }
        add(selectionPanel, "north")

        addPanel.apply {
            addButton("add_arrow", onAdd, "cell 0 1")
            addButton("add_all_arrow", onAddAll, "cell 0 2")
        }
        removePanel.apply {
            addButton("arrow_remove", onRemove, "cell 0 3")
            addButton("arrow_remove_all", onRemoveAll, "cell 0 4")
        }

        onAdd.subscribe {
            availableBotTable.selected().forEach { add(it) }
        }
        onAddAll.subscribe {
            availableBotTable.rows().forEach { add(it) }
        }
        onRemove.subscribe {
            selectedBotTable.selectedIndices().forEach { selectedBotTable.removeAt(it) }
        }
        onRemoveAll.subscribe {
            selectedBotTable.clear()
        }
        availableBotTable.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    val row = availableBotTable.rowAtPoint(e.point)
                    if (row >= 0) {
                        add(availableBotTable[row])
                    }
                }
            }
        })
        selectedBotTable.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    val row = selectedBotTable.rowAtPoint(e.point)
                    if (row >= 0) {
                        selectedBotTable.removeAt(row)
                    }
                }
            }
        })
    }

    private fun add(row: BotSelectionTableRow) {
        if (row.availability === BotAvailability.OFFLINE ||
            (row.availability === BotAvailability.READY && !selectedBotTable.contains(row.botInfo))
        ) {
            selectedBotTable.add(row)
        }
    }
}