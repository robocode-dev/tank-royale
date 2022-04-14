package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.booter.BootProcess
import dev.robocode.tankroyale.gui.booter.DirAndPid
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.Hints
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.SortedListModel
import dev.robocode.tankroyale.gui.ui.config.BotRootDirectoriesConfigDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showError
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onChanged
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onMultiClickedAtIndex
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onSelection
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.GuiTask.enqueue
import net.miginfocom.swing.MigLayout
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.*

object BotSelectionPanel : JPanel(MigLayout("insets 0", "[sg,grow][center][sg,grow]", "[grow][grow]")), FocusListener {

    private val onBootBots = Event<JButton>()
    private val onUnbootBots = Event<JButton>()

    private val onAdd = Event<JButton>()
    private val onAddAll = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onRemoveAll = Event<JButton>()

    private val botsDirectoryListModel = SortedListModel<BotInfo>()
    private val bootedBotListModel = SortedListModel<DirAndPid>()
    private val joinedBotListModel = SortedListModel<BotInfo>()
    private val selectedBotListModel = SortedListModel<BotInfo>()

    private val botsDirectoryList = createBotDirectoryList()
    private val bootedBotList = createRunningBotList()
    private val joinedBotList = createBotInfoList(joinedBotListModel)
    private val selectedBotList = createBotInfoList(selectedBotListModel)

    private val botsDirectoryScrollPane = JScrollPane(botsDirectoryList)
    private val bootedScrollPane = JScrollPane(bootedBotList)
    private val joinedBotsScrollPane = JScrollPane(joinedBotList)

    private val botsDirectoryPanel = createBotsDirectoryPanel()
    private val bootedBotsPanel = createRunningBotsPanel()
    private val joinedBotsPanel = createJoinedBotsPanel()
    private val selectBotsPanel = createSelectBotsPanel()

    private val bootButtonPanel = JPanel(MigLayout("fill, insets 0", "[fill]"))
    private val addPanel = JPanel(MigLayout("fill, insets 0", "[fill]"))
    private val removePanel = JPanel(MigLayout("fill, insets 0", "[fill]"))

    private val addRemoveButtonsPanel = createAddRemoveButtonsPanel()

    init {
        addFocusListener(this)
        isFocusable = true

        add(botsDirectoryPanel, "grow")
        add(bootButtonPanel)
        add(bootedBotsPanel, "grow, wrap")

        add(joinedBotsPanel, "grow")
        add(addRemoveButtonsPanel)
        add(selectBotsPanel, "grow")

        addBootButton()
        addUnbootButton()

        addAddButton()
        addAllButton()
        addRemoveButton()
        addRemoveAll()

        addToolTips()

        onBootBots.subscribe(this) { handleBootBots() }
        onUnbootBots.subscribe(this) { handleUnbootBots() }

        onAdd.subscribe(this) { handleAdd() }
        onAddAll.subscribe(this) { handleAddAll() }
        onRemove.subscribe(this) { handleRemove() }
        onRemoveAll.subscribe(this) { handleRemoveAll() }

        botsDirectoryList.onMultiClickedAtIndex { runFromBotDirectoryAtIndex(it) }
        joinedBotList.onMultiClickedAtIndex { addSelectedBotFromJoinedListAt(it) }
        selectedBotList.onMultiClickedAtIndex { removeSelectedBotAt(it) }

        botsDirectoryList.onSelection { BotSelectionEvents.onBotDirectorySelected.fire(it) }
        joinedBotList.onSelection { BotSelectionEvents.onJoinedBotSelected.fire(it) }
        selectedBotList.onSelection { BotSelectionEvents.onBotSelected.fire(it) }

        selectedBotList.onChanged { BotSelectionEvents.onSelectedBotListUpdated.fire(selectedBotListModel.list()) }

        ClientEvents.onBotListUpdate.subscribe(this) { updateJoinedBots() }

        BootProcess.onBootBot.subscribe(this) { updateBootingBot(it) }
        BootProcess.onUnbootBot.subscribe(this) { updateUnbootingBot(it) }

        ConfigSettings.onSaved.subscribe(this) { updateBotsDirectoryBots() }

        ServerEvents.onStopped.subscribe(this) { reset() }
    }

    private fun reset() {
        selectedBotListModel.clear()
        update()
    }

    private fun removeSelectedBotAt(index: Int) {
        if (index >= 0 && index < selectedBotListModel.size) {
            selectedBotListModel.removeElement(selectedBotListModel[index])
        }
    }

    private fun addSelectedBotFromJoinedListAt(index: Int) {
        if (index >= 0 && index < joinedBotListModel.size) {
            val botInfo = joinedBotListModel[index]
            if (!selectedBotListModel.contains(botInfo)) {
                selectedBotListModel.addElement(botInfo)
            }
        }
    }

    private fun runFromBotDirectoryAtIndex(index: Int) {
        if (index >= 0 && index < botsDirectoryListModel.size) {
            val botInfo = botsDirectoryListModel[index]
            BootProcess.run(listOf(botInfo.host))
        }
    }

    private fun createBotDirectoryList() =
        JList(botsDirectoryListModel).apply {
            cellRenderer = BotDirectoryListCellRenderer()
        }

    private fun createRunningBotList() =
        JList(bootedBotListModel).apply {
            cellRenderer = RunningBotCellRenderer()
        }

    private fun createBotInfoList(model: SortedListModel<BotInfo>) =
        JList(model).apply {
            cellRenderer = BotInfoListCellRenderer()
        }

    private fun handleBootBots() {
        val botDirs = botsDirectoryList.selectedIndices.map { botsDirectoryListModel[it].host }
        BootProcess.run(botDirs)
    }

    private fun handleUnbootBots() {
        val pidList = bootedBotList.selectedIndices.map { bootedBotListModel[it].pid }
        BootProcess.stop(pidList)
    }

    private fun handleAdd() {
        joinedBotList.selectedValuesList.forEach { botInfo ->
            if (!selectedBotListModel.contains(botInfo)) {
                selectedBotListModel.addElement(botInfo)
            }
        }
    }

    private fun handleAddAll() {
        for (i in 0 until joinedBotListModel.size) {
            val botInfo = joinedBotListModel[i]
            if (!selectedBotListModel.contains(botInfo)) {
                selectedBotListModel.addElement(botInfo)
            }
        }
    }

    private fun handleRemove() {
        selectedBotList.selectedValuesList.forEach {
            selectedBotListModel.removeElement(it)
        }
    }

    private fun handleRemoveAll() {
        selectedBotListModel.clear()
    }

    private fun addBootButton() {
        bootButtonPanel.addButton("boot_arrow", onBootBots, "cell 0 1").apply {
            isEnabled = false
            botsDirectoryList.onSelection {
                isEnabled = botsDirectoryList.selectedIndices.isNotEmpty()
            }
        }
    }

    private fun addUnbootButton() {
        bootButtonPanel.addButton("unboot_arrow", onUnbootBots, "cell 0 2").apply {
            isEnabled = false
            bootedBotList.onSelection {
                isEnabled = bootedBotList.selectedIndices.isNotEmpty()
            }
            bootedBotList.onChanged {
                bootedBotList.clearSelection()
                isEnabled = false
            }
        }
    }

    private fun addAddButton() {
        addPanel.addButton("add_arrow", onAdd, "cell 0 0").apply {
            isEnabled = false
            joinedBotList.onSelection {
                isEnabled = joinedBotList.selectedIndices.isNotEmpty()
            }
        }
    }

    private fun addAllButton() {
        addPanel.addButton("add_all_arrow", onAddAll, "cell 0 1").apply {
            isEnabled = false
            joinedBotList.onChanged {
                isEnabled = joinedBotList.model.size > 0
            }
        }
    }

    private fun addRemoveButton() {
        removePanel.addButton("arrow_remove", onRemove, "cell 0 0").apply {
            isEnabled = false
            selectedBotList.onSelection {
                isEnabled = selectedBotList.selectedIndices.isNotEmpty()
            }
            selectedBotList.onChanged {
                selectedBotList.clearSelection()
                isEnabled = false
            }
        }
    }

    private fun addRemoveAll() {
        removePanel.addButton("arrow_remove_all", onRemoveAll, "cell 0 1").apply {
            isEnabled = false
            selectedBotList.onChanged {
                isEnabled = selectedBotList.model.size > 0
            }
        }
    }

    private fun createAddRemoveButtonsPanel() =
        JPanel(MigLayout("", "[fill]", "[][5][]")).apply {
            add(addPanel, "cell 0 0")
            add(removePanel, "cell 0 2")
        }

    private fun createBotsDirectoryPanel() =
        JPanel(MigLayout("fill")).apply {
            add(botsDirectoryScrollPane, "grow")
            border = BorderFactory.createTitledBorder(Strings.get("bot_directories"))
        }

    private fun createRunningBotsPanel() =
        JPanel(MigLayout("fill")).apply {
            add(bootedScrollPane, "grow")
            border = BorderFactory.createTitledBorder(Strings.get("running_bots"))
        }

    private fun createJoinedBotsPanel() =
        JPanel(MigLayout("fill")).apply {
            add(joinedBotsScrollPane, "grow")
            border = BorderFactory.createTitledBorder(Strings.get("joined_bots"))
        }

    private fun createSelectBotsPanel() =
        JPanel(MigLayout("fill")).apply {
            add(JScrollPane(selectedBotList), "grow")
            border = BorderFactory.createTitledBorder(Strings.get("selected_bots"))
        }

    override fun focusGained(e: FocusEvent?) {
        update()
    }

    override fun focusLost(e: FocusEvent?) {}

    fun update() {
        updateBotsDirectoryBots()
        updateRunningBots()
        updateJoinedBots()

        enforceBotDirIsConfigured()
    }

    private fun updateBotsDirectoryBots() {
        botsDirectoryListModel.clear()

        BootProcess.info().forEach { botEntry ->
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
                    host = botEntry.dir, // host serves as filename here
                    port = -1
                )
            )

            enqueue {
                with(botsDirectoryScrollPane.horizontalScrollBar) {
                    value = maximum
                }
            }
        }
    }

    private fun enforceBotDirIsConfigured() {
        if (ConfigSettings.botDirectories.isEmpty()) {
            enqueue {
                with(BotRootDirectoriesConfigDialog) {
                    if (!isVisible) {
                        showError(Messages.get("no_bot_dir"))
                        isVisible = true
                    }
                }
            }
        }
    }

    private fun updateRunningBots() {
        enqueue {
            bootedBotListModel.apply {
                clear()
                BootProcess.runningBots.forEach { updateBootingBot(it) }
            }
        }
    }

    private fun updateJoinedBots() {
        enqueue {
            // Reset the list of joined bots to it matches the joined bots from the client
            joinedBotListModel.apply {
                clear()
                Client.joinedBots.forEach { botInfo ->
                    addElement(botInfo)
                }
            }
            with(joinedBotsScrollPane.horizontalScrollBar) {
                value = maximum
            }
        }
        enqueue {
            // Remove selected bots, if the bots are not on the joined bots from the client
            selectedBotListModel.apply {
                ArrayList(list()).forEach { botInfo -> // ArrayList is used for preventing ConcurrentModificationException
                    if (!Client.joinedBots.contains(botInfo)) {
                        removeElement(botInfo)
                    }
                }
            }
        }
    }

    private fun updateBootingBot(dirAndPid: DirAndPid) {
        bootedBotListModel.addElement(dirAndPid)

        enqueue {
            with(bootedScrollPane.horizontalScrollBar) {
                value = maximum
            }
        }
    }

    private fun updateUnbootingBot(dirAndPid: DirAndPid) {
        bootedBotListModel.removeElement(dirAndPid)
    }

    private fun addToolTips() {
        botsDirectoryList.toolTipText = Hints.get("new_battle.bot_directories")
        bootedBotList.toolTipText = Hints.get("new_battle.booted_bots")
        joinedBotList.toolTipText = Hints.get("new_battle.joined_bots")
        selectedBotList.toolTipText = Hints.get("new_battle.selected_bots")

        botsDirectoryPanel.toolTipText = botsDirectoryList.toolTipText
        bootedBotsPanel.toolTipText = bootedBotList.toolTipText
        joinedBotsPanel.toolTipText = joinedBotList.toolTipText
        selectBotsPanel.toolTipText = selectedBotList.toolTipText
    }
}