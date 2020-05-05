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


class SelectBotsPanel2(val onlySelectUnique: Boolean = false) : JPanel(MigLayout("fill")) {
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
            availableBotTable.selected().forEach { row ->
                if (row.availability === BotAvailability.OFFLINE ||
                    (row.availability === BotAvailability.READY && !selectedBotTable.contains(row.botInfo))
                ) {
                    selectedBotTable.add(row)
                }
            }
        }
/*
        onAddAll.subscribe {
            for (i in 0 until availableBotListModel.size) {
                val botInfo = availableBotListModel[i]
                if (!(onlySelectUnique && selectedBotListModel.contains(botInfo))) {
                    selectedBotListModel.addElement(botInfo)
                }
            }
        }
        onRemove.subscribe {
            selectedBotList.selectedValuesList.forEach {
                selectedBotListModel.removeElement(it)
            }
        }
        onRemoveAll.subscribe {
            selectedBotListModel.clear()
        }
        availableBotList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    val index = availableBotList.locationToIndex(e.point)
                    if (index >= 0 && index < availableBotListModel.size()) {
                        val botInfo = availableBotListModel[index]
                        if (!(onlySelectUnique && selectedBotListModel.contains(botInfo))) {
                            selectedBotListModel.addElement(botInfo)
                        }
                    }
                }
            }
        })
        selectedBotList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    val index = selectedBotList.locationToIndex(e.point)
                    if (index >= 0 && index < selectedBotListModel.size()) {
                        selectedBotListModel.removeElement(selectedBotListModel[index])
                    }
                }
            }
        })*/
    }
}