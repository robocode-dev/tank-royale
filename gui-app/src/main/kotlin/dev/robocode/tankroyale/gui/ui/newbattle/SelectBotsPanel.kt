package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.booter.BooterProcess
import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.settings.MiscSettings
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.ui.components.SortedListModel
import dev.robocode.tankroyale.gui.ui.config.BotDirectoryConfigDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showError
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosed
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

class SelectBotsPanel : JPanel(MigLayout("fill")), FocusListener {

    val botsDirectoryListModel = SortedListModel<BotInfo>()
    val joinedBotListModel = SortedListModel<BotInfo>()
    val selectedBotListModel = SortedListModel<BotInfo>()

    val botsDirectoryList = JList(botsDirectoryListModel)
    val joinedBotList = JList(joinedBotListModel)
    val selectedBotList = JList(selectedBotListModel)

    private val onBoot = Event<JButton>()

    private val onAdd = Event<JButton>()
    private val onAddAll = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onRemoveAll = Event<JButton>()

    init {
        addFocusListener(this)
        isFocusable = true

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

        val bootButton = bootButtonPanel.addButton("boot_arrow", onBoot)
        bootButton.isEnabled = false

        val addButton = addPanel.addButton("add_arrow", onAdd, "cell 0 1")
        addButton.isEnabled = false

        val addAllButton = addPanel.addButton("add_all_arrow", onAddAll, "cell 0 2")
        addAllButton.isEnabled = false

        val removeButton = removePanel.addButton("arrow_remove", onRemove, "cell 0 3")
        removeButton.isEnabled = false

        val removeAllButton = removePanel.addButton("arrow_remove_all", onRemoveAll, "cell 0 4")
        removeAllButton.isEnabled = false

        botsDirectoryList.cellRenderer = BotInfoListCellRenderer()
        joinedBotList.cellRenderer = BotInfoListCellRenderer()
        selectedBotList.cellRenderer = BotInfoListCellRenderer()

        botsDirectoryList.addListSelectionListener {
            bootButton.isEnabled = botsDirectoryList.selectedIndices.isNotEmpty()
        }

        joinedBotList.addListSelectionListener {
            addButton.isEnabled = joinedBotList.selectedIndices.isNotEmpty()
        }

        joinedBotList.model.addListDataListener(object: ListDataListener {
            override fun intervalAdded(e: ListDataEvent?) { update() }
            override fun intervalRemoved(e: ListDataEvent?) { update() }
            override fun contentsChanged(e: ListDataEvent?) { update() }
            fun update() {
                addAllButton.isEnabled = joinedBotList.model.size > 0
            }
        })

        selectedBotList.addListSelectionListener {
            removeButton.isEnabled = selectedBotList.selectedIndices.isNotEmpty()
        }

        selectedBotList.model.addListDataListener(object: ListDataListener {
            override fun intervalAdded(e: ListDataEvent?) { update() }
            override fun intervalRemoved(e: ListDataEvent?) { update() }
            override fun contentsChanged(e: ListDataEvent?) { update() }
            fun update() {
                removeAllButton.isEnabled = selectedBotList.model.size > 0
            }
        })

        onBoot.subscribe(this) {
            val files = ArrayList<String>()
            botsDirectoryList.selectedIndices.forEach {
                files.add(botsDirectoryListModel[it].host)
            }
            BooterProcess.run(files)
        }

        onAdd.subscribe(this) {
            joinedBotList.selectedValuesList.forEach { botInfo ->
                if (!selectedBotListModel.contains(botInfo)) {
                    selectedBotListModel.addElement(botInfo)
                }
            }
        }
        onAddAll.subscribe(this) {
            for (i in 0 until joinedBotListModel.size) {
                val botInfo = joinedBotListModel[i]
                if (!selectedBotListModel.contains(botInfo)) {
                    selectedBotListModel.addElement(botInfo)
                }
            }
        }
        onRemove.subscribe(this) {
            selectedBotList.selectedValuesList.forEach {
                selectedBotListModel.removeElement(it)
            }
        }
        onRemoveAll.subscribe(this) {
            selectedBotListModel.clear()
        }

        botsDirectoryList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    val index = botsDirectoryList.locationToIndex(e.point)
                    if (index >= 0 && index < botsDirectoryListModel.size) {
                        val botInfo = botsDirectoryListModel[index]
                        BooterProcess.run(listOf(botInfo.host))
                    }
                }
            }
        })
        joinedBotList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    val index = joinedBotList.locationToIndex(e.point)
                    if (index >= 0 && index < joinedBotListModel.size) {
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
                    if (index >= 0 && index < selectedBotListModel.size) {
                        selectedBotListModel.removeElement(selectedBotListModel[index])
                    }
                }
            }
        })
    }

    override fun focusGained(e: FocusEvent?) {
        enforceBotDirIsConfigured()
        updateBotsDirectoryBots()
    }

    override fun focusLost(e: FocusEvent?) {}

    private fun updateBotsDirectoryBots() {
        botsDirectoryListModel.clear()

        BooterProcess.list().forEach { botEntry ->
            val info = botEntry.info
            botsDirectoryListModel.addElement(
                BotInfo(
                    info.name,
                    info.version,
                    info.authors.split(","),
                    info.description,
                    info.homepage,
                    info.countryCodes.split(","),
                    info.gameTypes.split(",").toSet(),
                    info.platform,
                    info.programmingLang,
                    host = botEntry.filename, // host serves as filename here
                    port = -1
                )
            )
        }
    }

    private fun enforceBotDirIsConfigured() {
        if (MiscSettings.getBotDirectories().isEmpty()) {
            showError(ResourceBundles.MESSAGES.get("no_bot_dir"))

            BotDirectoryConfigDialog.onClosed {
                requestFocus() // onFocus() will be called
            }
            BotDirectoryConfigDialog.isVisible = true
        }
    }
}