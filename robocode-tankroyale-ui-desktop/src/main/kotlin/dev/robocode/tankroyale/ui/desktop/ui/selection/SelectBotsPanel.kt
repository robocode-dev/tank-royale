package dev.robocode.tankroyale.ui.desktop.ui.selection

import dev.robocode.tankroyale.ui.desktop.bootstrap.BootstrapProcess
import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.util.Event
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

@UnstableDefault
@ImplicitReflectionSerializer
class SelectBotsPanel : JPanel(MigLayout("fill")) {

    val botsDirectoryListModel = DefaultListModel<BotInfo>()
    val joinedBotListModel = DefaultListModel<BotInfo>()
    val selectedBotListModel = DefaultListModel<BotInfo>()

    val botsDirectoryList = JList(botsDirectoryListModel)
    val joinedBotList = JList(joinedBotListModel)
    val selectedBotList = JList(selectedBotListModel)

    private val onBoot = Event<JButton>()

    private val onAdd = Event<JButton>()
    private val onAddAll = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onRemoveAll = Event<JButton>()

    init {
        val botsDirectoryPanel = JPanel(MigLayout("fill")).apply {
            add(JScrollPane(botsDirectoryList), "grow")
            preferredSize = Dimension(1000, 1000)
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("bot_directory"))
        }

        val joinedBotsPanel = JPanel(MigLayout("fill")).apply {
            add(JScrollPane(joinedBotList), "grow")
            preferredSize = Dimension(1000, 1000)
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("joined_bots"))
        }

        val selectBotsPanel = JPanel(MigLayout("fill")).apply {
            add(JScrollPane(selectedBotList), "grow")
            preferredSize = Dimension(1000, 1000)
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
            add(botsDirectoryPanel, "grow")
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

        botsDirectoryList.cellRenderer = BotInfoListCellRenderer()
        joinedBotList.cellRenderer = BotInfoListCellRenderer()
        selectedBotList.cellRenderer = BotInfoListCellRenderer()

        onBoot.subscribe {
            val files = ArrayList<String>()
            botsDirectoryList.selectedIndices.forEach { files.add(botsDirectoryListModel.getElementAt(it).host) }

            BootstrapProcess.run(files)
        }

        onAdd.subscribe {
            joinedBotList.selectedValuesList.forEach { botInfo ->
                if (!selectedBotListModel.contains(botInfo)) {
                    selectedBotListModel.addElement(botInfo)
                }
            }
        }
        onAddAll.subscribe {
            for (i in 0 until joinedBotListModel.size) {
                val botInfo = joinedBotListModel[i]
                if (!selectedBotListModel.contains(botInfo)) {
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
        botsDirectoryList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    val index = botsDirectoryList.locationToIndex(e.point)
                    if (index >= 0 && index < botsDirectoryListModel.size()) {
                        val botInfo = botsDirectoryListModel[index]
                        BootstrapProcess.run(listOf(botInfo.host))
                    }
                }
            }
        })
        joinedBotList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    val index = joinedBotList.locationToIndex(e.point)
                    if (index >= 0 && index < joinedBotListModel.size()) {
                        val botInfo = joinedBotListModel[index]
                        if (!selectedBotListModel.contains(botInfo)) {
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