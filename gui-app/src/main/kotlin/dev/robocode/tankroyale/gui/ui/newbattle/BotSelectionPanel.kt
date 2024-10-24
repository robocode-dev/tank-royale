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
import dev.robocode.tankroyale.gui.ui.UiTitles
import dev.robocode.tankroyale.gui.ui.components.SortedListModel
import dev.robocode.tankroyale.gui.ui.config.BotRootDirectoriesConfigDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showError
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onChanged
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onMultiClickedAtIndex
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onSelection
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.EDT.enqueue
import net.miginfocom.swing.MigLayout
import java.awt.EventQueue
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.*

@SuppressWarnings("kotlin:S1192") // allow duplicated string literals
object BotSelectionPanel : JPanel(MigLayout("insets 0", "[sg,grow][center][sg,grow]", "[grow][grow]")), FocusListener {
    private fun readResolve(): Any = BotSelectionPanel

    private val onFilterDropdown = Event<JComboBox<String>>()

    private val onBootBots = Event<JButton>()
    private val onUnbootBots = Event<JButton>()
    private val onUnbootAllBots = Event<JButton>()

    private val onAdd = Event<JButton>()
    private val onAddAll = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onRemoveAll = Event<JButton>()

    private val filterAllString = Strings.get("bot_directories.filter.all")
    private val filterBotsOnlyString = Strings.get("bot_directories.filter.bots_only")
    private val filterTeamsOnlyString = Strings.get("bot_directories.filter.teams_only")

    private val filterDropdown = JComboBox(
        arrayOf(
            filterAllString,
            filterBotsOnlyString,
            filterTeamsOnlyString
        )
    )

    private val botsDirectoryListModel = SortedListModel<BotInfo>()
    private val bootedBotListModel = SortedListModel<DirAndPid>()
    private val joinedBotListModel = SortedListModel<BotInfo>()
    private val selectedBotListModel = SortedListModel<BotInfo>()

    private val botsDirectoryList = createBotDirectoryList()
    private val bootedBotList = createBootedBotList()
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
    private val filterDropdownPanel = JPanel(MigLayout("fill, insets 0", "[fill]"))

    private val addPanel = JPanel(MigLayout("fill, insets 0", "[fill]"))
    private val removePanel = JPanel(MigLayout("fill, insets 0", "[fill]"))

    private val filterAndBootButtonPanel = createFilterAndBootButtonPanel()
    private val addRemoveButtonsPanel = createAddRemoveButtonsPanel()

    init {
        addFocusListener(this)
        isFocusable = true

        add(botsDirectoryPanel, "grow")
        add(filterAndBootButtonPanel)
        add(bootedBotsPanel, "grow, wrap")

        add(joinedBotsPanel, "grow")
        add(addRemoveButtonsPanel)
        add(selectBotsPanel, "grow")

        addFilterLabel()
        addFilterComboBox()
        addBootButton()
        addUnbootButton()
        addUnbootAllButton()

        addAddButton()
        addAllButton()
        addRemoveButton()
        addRemoveAll()

        addToolTips()

        onFilterDropdown.subscribe(this) { handleFilterChanged() }

        onBootBots.subscribe(this) { handleBootBots() }
        onUnbootBots.subscribe(this) { handleUnbootBots() }
        onUnbootAllBots.subscribe(this) { handleUnbootAllBots() }

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

        BootProcess.onBootBot.subscribe(this) { addBootingBot(it) }
        BootProcess.onUnbootBot.subscribe(this) { removeUnbootingBot(it) }

        ConfigSettings.onSaved.subscribe(this) { updateBotsDirectoryEntries() }

        ServerEvents.onStopped.subscribe(this) { reset() }
    }

    private fun reset() {
        selectedBotListModel.clear()
        joinedBotListModel.clear()
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
            BootProcess.boot(listOf(botInfo.host))
        }
    }

    private fun createBotDirectoryList() =
        BotList(botsDirectoryListModel).apply {
            cellRenderer = BotDirectoryListCellRenderer()
        }

    private fun createBootedBotList() =
        BotList(bootedBotListModel, false).apply {
            cellRenderer = BootedBotCellRenderer()

            onDeleteKeyTyped.subscribe(BotSelectionPanel) { dirAndPids ->
                BootProcess.stop(dirAndPids.map { it.pid })
            }
        }

    private fun createBotInfoList(model: SortedListModel<BotInfo>) =
        BotList(model, false).apply {
            cellRenderer = BotInfoListCellRenderer()
        }

    private fun handleFilterChanged() {
        updateBotsDirectoryEntries()

        filterDropdown.model.selectedItem
    }

    private fun handleBootBots() {
        val botDirs = botsDirectoryList.selectedIndices.map { botsDirectoryListModel[it].host }
        BootProcess.boot(botDirs)
    }

    private fun handleUnbootBots() {
        val pids = bootedBotList.selectedIndices.map { bootedBotListModel[it].pid }
        BootProcess.stop(pids)
    }

    private fun handleUnbootAllBots() {
        BootProcess.stop(bootedBotListModel.list().map { it.pid })
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

    private fun addFilterLabel() {
        filterDropdownPanel.addLabel("directory_filter", "cell 0 0")
    }

    private fun addFilterComboBox() {
        filterDropdown.apply {
            addActionListener {
                onFilterDropdown.fire(this)
            }
            filterDropdownPanel.add(this, "cell 0 1")
        }
    }

    private fun addBootButton() {
        bootButtonPanel.addButton("boot_arrow", onBootBots, "cell 0 0").apply {
            isEnabled = false
            botsDirectoryList.onSelection {
                isEnabled = botsDirectoryList.selectedIndices.isNotEmpty()
            }
        }
    }

    private fun addUnbootButton() {
        bootButtonPanel.addButton("unboot_arrow", onUnbootBots, "cell 0 1").apply {
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

    private fun addUnbootAllButton() {
        bootButtonPanel.addButton("unboot_all_arrow", onUnbootAllBots, "cell 0 2").apply {
            isEnabled = false
            bootedBotList.onChanged {
                bootedBotList.clearSelection()
                isEnabled = bootedBotList.model.size > 0
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

    private fun createFilterAndBootButtonPanel() =
        JPanel(MigLayout("", "[fill]", "[][5][]")).apply {
            add(bootButtonPanel, "cell 0 0")
            add(filterDropdownPanel, "cell 0 2")
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

    override fun focusLost(e: FocusEvent?) {
        // Do nothing
    }

    fun update() {
        enqueue {
            updateBotsDirectoryEntries()
            updateRunningBots()
            updateJoinedBots()

            enforceBotDirIsConfigured()
        }
    }

    private fun updateBotsDirectoryEntries() {
        botsDirectoryList.clearSelection()
        botsDirectoryListModel.clear()

        val (botsOnly: Boolean, teamsOnly: Boolean) = when (filterDropdown.selectedItem) {
            filterBotsOnlyString -> Pair(true, false)
            filterTeamsOnlyString -> Pair(false, true)
            else -> Pair(false, false)
        }

        if (BootProcess.botDirs.isEmpty()) {
            JOptionPane.showMessageDialog(
                null,
                Messages.get("no_bot_directories_found"),
                UiTitles.get("error"),
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        BootProcess.info(botsOnly, teamsOnly).forEach { botEntry ->
            botEntry.apply {
                botsDirectoryListModel.addElement(
                    BotInfo(
                        name,
                        version,
                        authors,
                        description,
                        homepage,
                        countryCodes ?: emptyList(),
                        gameTypes?.toSet() ?: emptySet(),
                        platform,
                        programmingLang,
                        host = botEntry.dir // host serves as filename here
                    )
                )
            }
            EventQueue.invokeLater {
                botsDirectoryScrollPane.horizontalScrollBar.apply {
                    value = maximum
                }
            }
        }
    }

    private fun enforceBotDirIsConfigured() {
        if (ConfigSettings.botDirectories.isEmpty()) {
            EventQueue.invokeLater {
                with(BotRootDirectoriesConfigDialog) {
                    if (!isVisible) {
                        showError(Messages.get("no_bot_directories_found"))
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
                BootProcess.bootedBots.forEach { addBootingBot(it) }
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

    private fun addBootingBot(dirAndPid: DirAndPid) {
        bootedBotListModel.addElement(dirAndPid)

        EventQueue.invokeLater {
            bootedScrollPane.horizontalScrollBar.apply {
                value = maximum
            }
        }
    }

    private fun removeUnbootingBot(dirAndPid: DirAndPid) {
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