package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.booter.BootProcess
import dev.robocode.tankroyale.gui.booter.DirAndPid
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.model.InitialPosition
import dev.robocode.tankroyale.gui.settings.MiscSettings
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.ui.components.SortedListModel
import dev.robocode.tankroyale.gui.ui.config.BotRootDirectoriesConfigDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showError
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onChanged
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onMultiClickedAtIndex
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onSelection
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.EventQueue
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.*

object BotSelectionPanel : JPanel(MigLayout("", "[sg,grow][center][sg,grow]", "[grow][grow]")), FocusListener {

    private val onRunBots = Event<JButton>()
    private val onStopBots = Event<JButton>()

    private val onAdd = Event<JButton>()
    private val onAddAll = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onRemoveAll = Event<JButton>()

    private val botsDirectoryListModel = SortedListModel<BotInfo>()
    private val runningBotListModel = SortedListModel<DirAndPid>()
    private val joinedBotListModel = SortedListModel<BotInfo>()
    private val selectedBotListModel = SortedListModel<BotInfo>()

    private val botsDirectoryList = createBotDirectoryList()
    private val runningBotList = createRunningBotList()
    private val joinedBotList = createBotInfoList(joinedBotListModel)
    private val selectedBotList = createBotInfoList(selectedBotListModel)

    private val botsDirectoryScrollPane = JScrollPane(botsDirectoryList)
    private val runningScrollPane = JScrollPane(runningBotList)
    private val joinedBotsScrollPane = JScrollPane(joinedBotList)

    private val botsDirectoryPanel = createBotsDirectoryPanel()
    private val runningBotsPanel = createRunningBotsPanel()
    private val joinedBotsPanel = createJoinedBotsPanel()
    private val selectBotsPanel = createSelectBotsPanel()

    private val runButtonPanel = JPanel(MigLayout("fill, insets 0", "[fill]"))
    private val addPanel = JPanel(MigLayout("fill, insets 0", "[fill]"))
    private val removePanel = JPanel(MigLayout("fill, insets 0", "[fill]"))

    private val addRemoveButtonsPanel = createAddRemoveButtonsPanel()

    init {
        addFocusListener(this)
        isFocusable = true

        add(botsDirectoryPanel, "grow")
        add(runButtonPanel)
        add(runningBotsPanel, "grow, wrap")

        add(joinedBotsPanel, "grow")
        add(addRemoveButtonsPanel)
        add(selectBotsPanel, "grow")

        addRunButton()
        addStopButton()

        addAddButton()
        addAllButton()
        addRemoveButton()
        addRemoveAll()

        onRunBots.subscribe(this) { handleRunBots() }
        onStopBots.subscribe(this) { handleStopBots() }

        onAdd.subscribe(this) { handleAdd() }
        onAddAll.subscribe(this) { handleAddAll() }
        onRemove.subscribe(this) { handleRemove() }
        onRemoveAll.subscribe(this) { handleRemoveAll() }

        botsDirectoryList.onMultiClickedAtIndex { runFromBotDirectoryAtIndex(it) }
        joinedBotList.onMultiClickedAtIndex { addSelectedBotFromJoinedListAt(it) }
        selectedBotList.onMultiClickedAtIndex { removeSelectedBotAt(it) }

        botsDirectoryList.onSelection { BotSelectionChannel.onBotDirectorySelected.fire(it) }
        joinedBotList.onSelection { BotSelectionChannel.onJoinedBotSelected.fire(it) }
        selectedBotList.onSelection { BotSelectionChannel.onBotSelected.fire(it) }

        selectedBotList.onChanged { BotSelectionChannel.onSelectedBotListUpdated.fire(selectedBotListModel.list()) }

        Client.onBotListUpdate.subscribe(this) { updateJoinedBots() }

        BootProcess.onRunBot.subscribe(this) { updateRunningBot(it) }
        BootProcess.onStopBot.subscribe(this) { updateStoppingBot(it) }

        MiscSettings.onSaved.subscribe(this) { updateBotsDirectoryBots() }
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
        JList(runningBotListModel).apply {
            cellRenderer = RunningBotCellRenderer()
        }

    private fun createBotInfoList(model: SortedListModel<BotInfo>) =
        JList(model).apply {
            cellRenderer = BotInfoListCellRenderer()
        }

    private fun handleRunBots() {
        val botDirs = botsDirectoryList.selectedIndices.map { botsDirectoryListModel[it].host }
        BootProcess.run(botDirs)
    }

    private fun handleStopBots() {
        val pidList = runningBotList.selectedIndices.map { runningBotListModel[it].pid }
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

    private fun addRunButton() {
        runButtonPanel.addButton("run_arrow", onRunBots, "cell 0 1").apply {
            isEnabled = false
            botsDirectoryList.onSelection {
                isEnabled = botsDirectoryList.selectedIndices.isNotEmpty()
            }
        }
    }

    private fun addStopButton() {
        runButtonPanel.addButton("stop_arrow", onStopBots, "cell 0 2").apply {
            isEnabled = false
            runningBotList.onSelection {
                isEnabled = runningBotList.selectedIndices.isNotEmpty()
            }
            runningBotList.onChanged {
                runningBotList.clearSelection()
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
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("bot_directories"))
        }

    private fun createRunningBotsPanel() =
        JPanel(MigLayout("fill")).apply {
            add(runningScrollPane, "grow")
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("running_bots"))
        }

    private fun createJoinedBotsPanel() =
        JPanel(MigLayout("fill")).apply {
            add(joinedBotsScrollPane, "grow")
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("joined_bots"))
        }

    private fun createSelectBotsPanel() =
        JPanel(MigLayout("fill")).apply {
            add(JScrollPane(selectedBotList), "grow")
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("selected_bots"))
        }

    override fun focusGained(e: FocusEvent?) {
        update()
    }

    override fun focusLost(e: FocusEvent?) {}

    fun update() {
        updateBotsDirectoryBots()
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
                    InitialPosition.fromString(info.initialPosition),
                    host = botEntry.dir, // host serves as filename here
                    port = -1
                )
            )

            SwingUtilities.invokeLater {
                with(botsDirectoryScrollPane.horizontalScrollBar) {
                    value = maximum
                }
            }
        }
    }

    private fun enforceBotDirIsConfigured() {
        if (MiscSettings.getBotDirectories().isEmpty()) {
            SwingUtilities.invokeLater {
                with(BotRootDirectoriesConfigDialog) {
                    if (!isVisible) {
                        showError(ResourceBundles.MESSAGES.get("no_bot_dir"))
                        isVisible = true
                    }
                }
            }
        }
    }

    private fun updateJoinedBots() {
        SwingUtilities.invokeLater {
            // Reset the list of joined bots to it matches the joined bots from the client
            joinedBotListModel.apply {
                EventQueue.invokeLater {
                    clear()
                    Client.joinedBots.forEach { addElement(it) }
                }
            }
            // Remove selected bots, if the bots are not on the joined bots from the client
            selectedBotListModel.apply {
                list().forEach {
                    EventQueue.invokeLater {
                        if (!Client.joinedBots.contains(it)) { removeElement(it) }
                    }
                }
            }
            with(joinedBotsScrollPane.horizontalScrollBar) {
                value = maximum
            }
        }
    }

    private fun updateRunningBot(dirAndPid: DirAndPid) {
        runningBotListModel.addElement(dirAndPid)

        SwingUtilities.invokeLater {
            with(runningScrollPane.horizontalScrollBar) {
                value = maximum
            }
        }
    }

    private fun updateStoppingBot(dirAndPid: DirAndPid) {
        runningBotListModel.removeElement(dirAndPid)
    }
}