package dev.robocode.tankroyale.ui.desktop.ui.selection

import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class SelectBotsPanel(val onlySelectUnique: Boolean = false) : JPanel(MigLayout("fill")) {

    private val offlineBotListModel = DefaultListModel<BotInfo>()
    private val joinedBotListModel = DefaultListModel<BotInfo>()
    private val selectedBotListModel = DefaultListModel<BotInfo>()

    private val offlineBotList = JList(offlineBotListModel)
    val joinedBotList = JList(joinedBotListModel)
    val selectedBotList = JList(selectedBotListModel)

    private val onBoot = Event<JButton>()

    private val onAdd = Event<JButton>()
    private val onAddAll = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onRemoveAll = Event<JButton>()

    init {
        val offlineBotsPanel = JPanel(MigLayout("fill")).apply {
            add(JScrollPane(offlineBotList), "grow")
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("offline_bots"))
        }

        val joinedBotsPanel = JPanel(MigLayout("fill")).apply {
            add(JScrollPane(joinedBotList), "grow")
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("joined_bots"))
        }

        val selectBotsPanel = JPanel(MigLayout("fill")).apply {
            add(JScrollPane(selectedBotList), "grow")
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("selected_bots"))
        }

        val bootButtonPanel = JPanel(MigLayout("fill", "[fill]"))

        val addPanel = JPanel(MigLayout("fill", "[fill]"))
        val middlePanel = JPanel(MigLayout("fill"))
        val removePanel = JPanel(MigLayout("fill", "[fill]"))

        val addRemoveButtonsPanel = JPanel(MigLayout()).apply {
            add(addPanel, "north")
            add(middlePanel, "h 300")
            add(removePanel, "south")
        }
        val selectionPanel = JPanel(MigLayout("", "[grow][grow][][grow]")).apply {
            add(offlineBotsPanel, "grow")
            add(bootButtonPanel, "")
            add(joinedBotsPanel, "grow")
            add(addRemoveButtonsPanel, "")
            add(selectBotsPanel, "grow")
        }
        add(selectionPanel, "north")

        bootButtonPanel.addButton("boot_arrow", onBoot)

        addPanel.apply {
            addButton("add_arrow", onAdd, "cell 0 1")
            addButton("add_all_arrow", onAddAll, "cell 0 2")
        }
        removePanel.apply {
            addButton("arrow_remove", onRemove, "cell 0 3")
            addButton("arrow_remove_all", onRemoveAll, "cell 0 4")
        }

        joinedBotList.cellRenderer = BotInfoListCellRenderer()
        selectedBotList.cellRenderer = BotInfoListCellRenderer()

        onAdd.subscribe {
            joinedBotList.selectedValuesList.forEach { botInfo ->
                if (!(onlySelectUnique && selectedBotListModel.contains(botInfo))) {
                    selectedBotListModel.addElement(botInfo)
                }
            }
        }
        onAddAll.subscribe {
            for (i in 0 until joinedBotListModel.size) {
                val botInfo = joinedBotListModel[i]
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
        joinedBotList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    val index = joinedBotList.locationToIndex(e.point)
                    if (index >= 0 && index < joinedBotListModel.size()) {
                        val botInfo = joinedBotListModel[index]
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
        })
    }
}