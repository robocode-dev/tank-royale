package dev.robocode.tankroyale.ui.desktop.ui.selection

import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane

class SelectBotsPanel2 : JPanel(MigLayout("fill")) {
    private val onAdd = Event<JButton>()
    private val onAddAll = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onRemoveAll = Event<JButton>()

    val joinedBotTable = BotSelectionTable()
    val selectedBotTable = BotSelectionTable()

    init {
        val leftSelectionPanel = JPanel(MigLayout("fill")).apply {
            val scrollPane = JScrollPane(joinedBotTable)
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
            joinedBotTable.selected().forEach { add(it) }
        }
        onAddAll.subscribe {
            joinedBotTable.rows().forEach { add(it) }
        }
        onRemove.subscribe {
            selectedBotTable.selectedIndices().forEach { selectedBotTable.removeAt(it) }
        }
        onRemoveAll.subscribe {
            selectedBotTable.clear()
        }
        joinedBotTable.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    val row = joinedBotTable.rowAtPoint(e.point)
                    if (row >= 0) {
                        add(joinedBotTable[row])
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