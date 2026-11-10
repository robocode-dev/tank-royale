package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.client.model.BotInfo
import dev.robocode.tankroyale.common.event.Event
import dev.robocode.tankroyale.gui.booter.BootProcess
import dev.robocode.tankroyale.gui.booter.DirAndPid
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.Hints
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.SortedListModel
import dev.robocode.tankroyale.gui.ui.config.BotRootDirectoriesConfigDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showError
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onChanged
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onMultiClickedAtIndex
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onSelection
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import dev.robocode.tankroyale.gui.util.EDT.enqueue
import dev.robocode.tankroyale.gui.util.RefreshWhenShown
import dev.robocode.tankroyale.rumble.BotIdentity
import dev.robocode.tankroyale.rumble.DownloadFolders
import dev.robocode.tankroyale.rumble.KnownLicenses
import net.miginfocom.swing.MigLayout
import java.awt.EventQueue
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.*

@SuppressWarnings("kotlin:S1192") // allow duplicated string literals
object BotSelectionPanel : JPanel(MigLayout("insets 0", "[sg,grow][center][sg,grow]", "[grow][grow]")), FocusListener {
    private fun readResolve(): Any = BotSelectionPanel

    private val bootCoordinator = BotBootCoordinator { SwingUtilities.getWindowAncestor(this) }

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

    // The bots of bot directories labelled as downloaded from the Rumble, shown under the person's own bots below a
    // header row that opens and closes the group. The header sits outside the sideways-scrolled list of long paths, so
    // it is always in view. The group starts closed, so the person's own bots come first.
    private val downloadedListModel = SortedListModel<BotInfo>()
    // The bot's own folder name is shown, as the header row already says where the downloaded bots are, or its whole
    // path when two bots in the group have folders of the same name. The names are worked out when the list is read.
    private var downloadedNames: Map<String, String> = emptyMap()
    private val downloadedList = BotList(downloadedListModel).apply {
        cellRenderer = object : AbstractListCellRenderer() {
            override fun onRender(list: JList<out Any>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean) {
                val host = (value as BotInfo).host
                text = downloadedNames[host] ?: java.io.File(host).name
            }
        }
    }

    // The licenses of every bot in the bot directories for the game type, whatever the filter shows, for bots that join on their own.
    private var knownLicenses = KnownLicenses(emptyList())

    // A change of the settings made while New Battle is closed, such as a download adding its folder, is read when New
    // Battle opens, so the download screen never waits for the bot directories to be read. While New Battle is open,
    // it is read once the screen that saved has been drawn.
    private val refreshDirectories = RefreshWhenShown({ isShowing }) { enqueue { updateBotsDirectoryEntries() } }
    private var downloadedOpen = false
    private val downloadedHeader = JButton().apply {
        isBorderPainted = false
        isContentAreaFilled = false
        isFocusPainted = false
        horizontalAlignment = SwingConstants.LEFT
        addActionListener { showDownloaded(!downloadedOpen) }
    }
    private val downloadedScrollPane = JScrollPane(downloadedList)
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

        onFilterDropdown.on(this) { handleFilterChanged() }

        onBootBots.on(this) { handleBootBots() }
        onUnbootBots.on(this) { handleUnbootBots() }
        onUnbootAllBots.on(this) { handleUnbootAllBots() }

        onAdd.on(this) { handleAdd() }
        onAddAll.on(this) { handleAddAll() }
        onRemove.on(this) { handleRemove() }
        onRemoveAll.on(this) { handleRemoveAll() }

        botsDirectoryList.onMultiClickedAtIndex { runFromBotDirectoryAtIndex(botsDirectoryListModel, it) }
        downloadedList.onMultiClickedAtIndex { runFromBotDirectoryAtIndex(downloadedListModel, it) }
        downloadedList.onSelection { BotSelectionEvents.onBotDirectorySelected(it) }
        // One selection at a time across the person's own bots and the downloaded ones, so Boot boots what is seen selected.
        botsDirectoryList.addListSelectionListener { if (!botsDirectoryList.isSelectionEmpty) downloadedList.clearSelection() }
        downloadedList.addListSelectionListener { if (!downloadedList.isSelectionEmpty) botsDirectoryList.clearSelection() }
        showDownloaded(false)
        joinedBotList.onMultiClickedAtIndex { addSelectedBotFromJoinedListAt(it) }
        selectedBotList.onMultiClickedAtIndex { removeSelectedBotAt(it) }

        botsDirectoryList.onSelection { BotSelectionEvents.onBotDirectorySelected(it) }
        joinedBotList.onSelection { BotSelectionEvents.onJoinedBotSelected(withKnownLicense(it)) }
        selectedBotList.onSelection { BotSelectionEvents.onBotSelected(withKnownLicense(it)) }

        selectedBotList.onChanged { BotSelectionEvents.onSelectedBotListUpdated(selectedBotListModel.list()) }

        ClientEvents.onBotListUpdate.on(this) { updateJoinedBots() }

        BootProcess.onBootBot.on(this) { addBootingBot(it) }
        BootProcess.onUnbootBot.on(this) { removeUnbootingBot(it) }

        ConfigSettings.onSaved.on(this) { refreshDirectories.changed() }

        ServerEvents.onStopped.on(this) { reset() }
    }

    private fun reset() {
        bootCoordinator.reset()
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

    private fun runFromBotDirectoryAtIndex(model: SortedListModel<BotInfo>, index: Int) {
        if (index >= 0 && index < model.size) {
            bootCoordinator.bootBots(listOf(model[index]))
        }
    }

    private fun bootAndShowProgress(botInfoList: List<BotInfo>) {
        bootCoordinator.bootBots(botInfoList)
    }

    private fun createBotDirectoryList() =
        BotList(botsDirectoryListModel).apply {
            cellRenderer = BotDirectoryListCellRenderer()
        }

    private fun createBootedBotList() =
        BotList(bootedBotListModel, false).apply {
            cellRenderer = BootedBotCellRenderer()

            onDeleteKeyTyped.on(BotSelectionPanel) { dirAndPids ->
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

    /** Opens or closes the group of downloaded bots, and shows how many there are on its header row. */
    private fun showDownloaded(open: Boolean) {
        downloadedOpen = open
        // A closed group keeps no selection, so Boot never boots a bot the person cannot see.
        if (!open) downloadedList.clearSelection()
        downloadedScrollPane.isVisible = open && downloadedListModel.size > 0
        downloadedHeader.isVisible = downloadedListModel.size > 0
        downloadedHeader.icon = UIManager.getIcon(if (open) "Tree.expandedIcon" else "Tree.collapsedIcon")
        downloadedHeader.text = String.format(Strings.get("rumble_downloads_group"), Strings.get("rumble_downloads_label"), downloadedListModel.size)
        botsDirectoryPanel.revalidate()
        botsDirectoryPanel.repaint()
    }

    /**
     * A bot that joined the battle on its own brings no license, so it is taken from the bot of the same name, version and
     * authors in the bot directories. When there is none, or several that disagree, the license stays not known.
     */
    private fun withKnownLicense(botInfo: BotInfo): BotInfo {
        if (botInfo.license != null) return botInfo
        return knownLicenses.of(BotIdentity(botInfo.name, botInfo.version, botInfo.authors))?.let { botInfo.copy(license = it) } ?: botInfo
    }

    private fun handleBootBots() {
        val selected = botsDirectoryList.selectedValuesList + downloadedList.selectedValuesList
        if (selected.isEmpty()) return
        bootAndShowProgress(selected)
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
                onFilterDropdown(this)
            }
            filterDropdownPanel.add(this, "cell 0 1")
        }
    }

    private fun addBootButton() {
        bootButtonPanel.addButton("boot_arrow", onBootBots, "cell 0 0").apply {
            isEnabled = false
            // Boot is offered for a selection in either the person's own bots or the downloaded ones.
            val update = { isEnabled = !botsDirectoryList.isSelectionEmpty || !downloadedList.isSelectionEmpty }
            botsDirectoryList.addListSelectionListener { update() }
            downloadedList.addListSelectionListener { update() }
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
        JPanel(MigLayout("fill, hidemode 3, wrap 1, gapy 2", "[grow, fill]", "[grow][][grow]")).apply {
            add(botsDirectoryScrollPane, "grow")
            add(downloadedHeader, "growx")
            add(downloadedScrollPane, "grow")
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
        downloadedList.clearSelection()
        downloadedListModel.clear()
        val labels = ConfigSettings.botDirectoryLabels

        val (botsOnly: Boolean, teamsOnly: Boolean) = when (filterDropdown.selectedItem) {
            filterBotsOnlyString -> Pair(true, false)
            filterTeamsOnlyString -> Pair(false, true)
            else -> Pair(false, false)
        }

        if (BootProcess.botDirs.isEmpty()) {
            showNoNotDirectoriesFoundErrorAndShowBotRootDirectoriesConfig()
            return
        }

        // Every bot for the game type is read, so the licenses are known whatever the filter shows; the filter is applied
        // here the same way the booter applies it.
        val entries = BootProcess.info()
        knownLicenses = KnownLicenses(entries.map { BotIdentity(it.name, it.version, it.authors) to it.license })
        entries.filter { entry ->
            val isTeam = entry.teamMembers?.isNotEmpty() == true
            !(botsOnly && isTeam) && !(teamsOnly && !isTeam)
        }.forEach { botEntry ->
            val model = if (DownloadFolders.isDownloaded(botEntry.dir, labels)) downloadedListModel else botsDirectoryListModel
            botEntry.apply {
                model.addElement(
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
                        host = botEntry.dir, // host serves as filename here
                        license = license ?: "",
                    )
                )
            }
            EventQueue.invokeLater {
                botsDirectoryScrollPane.horizontalScrollBar.apply {
                    value = maximum
                }
            }
        }
        downloadedNames = DownloadFolders.shownNames(downloadedListModel.list().map { it.host })
        showDownloaded(downloadedOpen)
    }

    /**
     * New Battle is opening: the group of downloaded bots is closed, so the person's own bots come first, and the bot
     * directories are read again when the settings changed while New Battle was closed.
     */
    fun opened() {
        showDownloaded(false)
        refreshDirectories.shown()
    }

    private fun enforceBotDirIsConfigured() {
        if (ConfigSettings.botDirectories.isEmpty()) {
            showNoNotDirectoriesFoundErrorAndShowBotRootDirectoriesConfig()
        }
    }

    private var isShowingNoNotDirectoriesFoundError = false

    private fun showNoNotDirectoriesFoundErrorAndShowBotRootDirectoriesConfig() {
        EventQueue.invokeLater {
            if (!isShowingNoNotDirectoriesFoundError) {
                isShowingNoNotDirectoriesFoundError = true

                showError(Messages.get("no_bot_directories_found"))
                BotRootDirectoriesConfigDialog.isVisible = true

                isShowingNoNotDirectoriesFoundError = false
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
