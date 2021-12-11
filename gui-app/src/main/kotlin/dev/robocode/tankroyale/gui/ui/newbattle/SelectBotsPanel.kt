package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.booter.BooterProcess
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.settings.MiscSettings
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.ui.components.SortedListModel
import dev.robocode.tankroyale.gui.ui.config.BotDirectoryConfigDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showError
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onChanged
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onSelection
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosed
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

object SelectBotsPanel : JPanel(MigLayout("fill")), FocusListener {

    private val onBoot = Event<JButton>()

    private val onAdd = Event<JButton>()
    private val onAddAll = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onRemoveAll = Event<JButton>()

    private val botsDirectoryListModel = SortedListModel<BotInfo>()
    private val joinedBotListModel = SortedListModel<BotInfo>()
    private val selectedBotListModel = SortedListModel<BotInfo>()

    private val botsDirectoryList = JList(botsDirectoryListModel)
    private val joinedBotList = JList(joinedBotListModel)
    private val selectedBotList = JList(selectedBotListModel)

    private val botsDirectoryPanel = createBotsDirectoryPanel()
    private val joinedBotsPanel = createJoinedBotsPanel()
    private val selectBotsPanel = createSelectBotsPanel()

    private val bootButtonPanel = JPanel(MigLayout("fill", "[fill]"))
    private val addPanel = JPanel(MigLayout("fill", "[fill]"))
    private val middlePanel = JPanel(MigLayout("fill"))
    private val removePanel = JPanel(MigLayout("fill", "[fill]"))

    private val addRemoveButtonsPanel = createAddRemoveButtonsPanel()

    init {
        addFocusListener(this)
        isFocusable = true

        add(createSelectionPanel(), "north")

        bootButtonPanel.addButton("boot_arrow", onBoot).apply {
            isEnabled = false
            botsDirectoryList.onSelection {
                isEnabled = botsDirectoryList.selectedIndices.isNotEmpty()
            }
        }
        addPanel.addButton("add_arrow", onAdd, "cell 0 1").apply {
            isEnabled = false
            joinedBotList.onSelection {
                isEnabled = joinedBotList.selectedIndices.isNotEmpty()
            }
        }
        addPanel.addButton("add_all_arrow", onAddAll, "cell 0 2").apply {
            isEnabled = false
            joinedBotList.onChanged {
                isEnabled = joinedBotList.model.size > 0
            }
        }
        removePanel.addButton("arrow_remove", onRemove, "cell 0 3").apply {
            isEnabled = false
            selectedBotList.onSelection {
                isEnabled = selectedBotList.selectedIndices.isNotEmpty()
            }
        }
        removePanel.addButton("arrow_remove_all", onRemoveAll, "cell 0 4").apply {
            isEnabled = false
            selectedBotList.onChanged {
                isEnabled = selectedBotList.model.size > 0
            }
        }

        botsDirectoryList.cellRenderer = BotInfoListCellRenderer()
        joinedBotList.cellRenderer = BotInfoListCellRenderer()
        selectedBotList.cellRenderer = BotInfoListCellRenderer()

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

        Client.onBotListUpdate.subscribe(NewBattleDialog) { updateJoinedBots() }

        botsDirectoryList.onSelection { BotSelectionChannel.onBotDirectorySelected.fire(it) }
        joinedBotList.onSelection { BotSelectionChannel.onJoinedBotSelected.fire(it) }
        selectedBotList.onSelection { BotSelectionChannel.onBotSelected.fire(it) }

        selectedBotList.onChanged { BotSelectionChannel.onSelectedBotListUpdated.fire(selectedBotListModel.list()) }
    }

    private fun createSelectionPanel() =
        JPanel(MigLayout("", "[grow][grow][][grow]")).apply {
            add(botsDirectoryPanel, "grow")
            add(bootButtonPanel, "")
            add(joinedBotsPanel, "grow")
            add(addRemoveButtonsPanel, "")
            add(selectBotsPanel, "grow")
        }

    private fun createAddRemoveButtonsPanel() =
        JPanel(MigLayout()).apply {
            add(addPanel, "north")
            add(middlePanel, "h 300")
            add(removePanel, "south")
        }

    private fun createSelectBotsPanel() =
        JPanel(MigLayout("fill")).apply {
            add(JScrollPane(selectedBotList), "grow")
            preferredSize = Dimension(1000, 1000)
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("selected_bots"))
        }

    private fun createJoinedBotsPanel() =
        JPanel(MigLayout("fill")).apply {
            add(JScrollPane(joinedBotList), "grow")
            preferredSize = Dimension(1000, 1000)
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("joined_bots"))
        }

    private fun createBotsDirectoryPanel() =
        JPanel(MigLayout("fill")).apply {
            add(JScrollPane(botsDirectoryList), "grow")
            preferredSize = Dimension(1000, 1000)
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("bot_directory"))
        }

    override fun focusGained(e: FocusEvent?) {
        updateBotsDirectoryBots()
        updateJoinedBots()

        enforceBotDirIsConfigured()
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
                requestFocus() // focusGained() will be called
            }
            BotDirectoryConfigDialog.isVisible = true
        }
    }

    private fun updateJoinedBots() {
        SwingUtilities.invokeLater {
            joinedBotListModel.apply {
                clear()
                Client.joinedBots.forEach { addElement(it) }
            }
        }
    }
}