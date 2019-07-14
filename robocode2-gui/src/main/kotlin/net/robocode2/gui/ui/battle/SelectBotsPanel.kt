package net.robocode2.gui.ui.battle

import kotlinx.serialization.ImplicitReflectionSerializer
import net.miginfocom.swing.MigLayout
import net.robocode2.gui.bootstrap.BootstrapProcess
import net.robocode2.gui.bootstrap.BotEntry
import net.robocode2.gui.client.Client
import net.robocode2.gui.extensions.JComponentExt.addNewButton
import net.robocode2.gui.extensions.JComponentExt.addNewLabel
import net.robocode2.gui.model.GameSetup
import net.robocode2.gui.settings.ServerSettings
import net.robocode2.gui.ui.ResourceBundles.STRINGS
import net.robocode2.gui.utils.Event
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.EmptyBorder


@ImplicitReflectionSerializer
object SelectBotsPanel : JPanel(MigLayout("fill")) {

    // Private events
    private val onStartBattle = Event<JButton>()
    private val onConnectButtonClicked = Event<JButton>()

    private val onCancel = Event<JButton>()
    private val onAdd = Event<JButton>()
    private val onAddAll = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onRemoveAll = Event<JButton>()

    private val gameTypeComboBox = GameTypeComboBox()

    private val availableBotListModel = DefaultListModel<BotEntry>()
    private val selectedBotListModel = DefaultListModel<BotEntry>()
    private val availableBotList = JList<BotEntry>(availableBotListModel)
    private val selectedBotList = JList<BotEntry>(selectedBotListModel)

    private val connectionStatusLabel = JLabel(connectionStatus)

    private val connectionStatus: String
        get() = if (Client.isConnected) STRINGS.get("connected") else STRINGS.get("disconnected")

    val gameSetup: GameSetup
        get() = gameTypeComboBox.mutableGameSetup.toGameSetup()

    init {
        val upperPanel = JPanel(MigLayout("", "[][grow][]"))
        val lowerPanel = JPanel(MigLayout("insets 10, fill"))

        add(upperPanel, "north")
        add(lowerPanel, "south, h 1000000")

        upperPanel.addNewLabel("game_type")
        upperPanel.add(gameTypeComboBox)
        upperPanel.addNewLabel("connection_status", "right")
        upperPanel.add(connectionStatusLabel, "center")

        val selectionPanel = JPanel(MigLayout("", "[grow][][grow]"))
        val buttonPanel = JPanel(MigLayout("center, insets 0"))

        lowerPanel.add(selectionPanel, "north")
        lowerPanel.add(buttonPanel, "center")

        val leftSelectionPanel = JPanel(MigLayout("fill"))
        leftSelectionPanel.add(JScrollPane(availableBotList), "grow")

        val rightSelectionPanel = JPanel(MigLayout("fill"))
        rightSelectionPanel.add(JScrollPane(selectedBotList), "grow")

        val centerSelectionPanel = JPanel(MigLayout("insets 0"))

        // Sets the preferred size to avoid right panel with to grow much larger than the right panel
        leftSelectionPanel.preferredSize = Dimension(10, 10)
        rightSelectionPanel.preferredSize = Dimension(10, 10)

        selectionPanel.add(leftSelectionPanel, "grow")
        selectionPanel.add(centerSelectionPanel, "")
        selectionPanel.add(rightSelectionPanel, "grow")

        leftSelectionPanel.border = BorderFactory.createTitledBorder(STRINGS.get("available_bots"))
        rightSelectionPanel.border = BorderFactory.createTitledBorder(STRINGS.get("selected_bots"))

        val addPanel = JPanel(MigLayout("insets 0, fill", "[fill]"))
        val removePanel = JPanel(MigLayout("insets 0, fill", "[fill]"))

        val middlePanel = JPanel(MigLayout("fill"))

        centerSelectionPanel.add(addPanel, "north")
        centerSelectionPanel.add(middlePanel, "h 300")
        centerSelectionPanel.add(removePanel, "south")

        addPanel.addNewButton("add_arrow", onAdd, "cell 0 1")
        addPanel.addNewButton("add_all_arrow", onAddAll, "cell 0 2")
        removePanel.addNewButton("arrow_remove", onRemove, "cell 0 3")
        removePanel.addNewButton("arrow_remove_all", onRemoveAll, "cell 0 4")

        buttonPanel.addNewButton("start_battle", onStartBattle, "tag ok")
        buttonPanel.addNewButton("cancel", onCancel, "tag cancel")

        availableBotList.cellRenderer = BotEntryCellRenderer()
        selectedBotList.cellRenderer = BotEntryCellRenderer()

        onConnectButtonClicked.subscribe {
            if (Client.isConnected) {
                Client.close()
            } else {
                Client.connect(ServerSettings.endpoint)
            }
            updateConnectionState()
        }

        onCancel.subscribe { BattleDialog.dispose() }

        onAdd.subscribe {
            availableBotList.selectedValuesList.forEach {
                selectedBotListModel.addElement(it)
            }
        }
        onAddAll.subscribe {
            for (i in 0 until availableBotListModel.size) {
                selectedBotListModel.addElement(availableBotListModel[i])
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
                if (e.clickCount == 2) {
                    val index = availableBotList.locationToIndex(e.point)
                    selectedBotListModel.addElement(availableBotListModel[index])
                }
            }
        })
        selectedBotList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val index = selectedBotList.locationToIndex(e.point)
                    selectedBotListModel.removeElement(selectedBotListModel[index])
                }
            }
        })

        Client.onConnected.subscribe { updateConnectionState() }
        Client.onDisconnected.subscribe { updateConnectionState() }

        onStartBattle.subscribe { startGame() }

        availableBotListModel.clear()
        BootstrapProcess.list().forEach { availableBotListModel.addElement(it) }
    }

    private fun updateConnectionState() {
        connectionStatusLabel.text = connectionStatus
    }

    private fun startGame() {
        StartGameWindow.isVisible = true

        val botEntries = ArrayList<String>()
        selectedBotListModel.toArray().forEach { b -> botEntries += (b as BotEntry).filename }

        BootstrapProcess.run(botEntries)

        StartGameWindow.isVisible = true
        BattleDialog.dispose()
/*
        Client.onBotListUpdate.subscribe { botListUpdate ->
            run {
                if (botListUpdate.bots.size == botEntries.size) { // FIXME: Show dialog instead with running bots and failing bots. Let user decide when to run battle
                    val selectedBotAddresses = HashSet<BotAddress>()
                    botListUpdate.bots.forEach { botInfo -> selectedBotAddresses += botInfo.botAddress }

                    Client.onGameStarted.subscribe { BattleDialog.dispose() }
                    Client.onGameAborted.subscribe { BootstrapProcess.stopRunning() }
                    Client.onGameEnded.subscribe { BootstrapProcess.stopRunning() }

                    Client.startGame(gameTypeComboBox.mutableGameSetup.toGameSetup(), selectedBotAddresses)
                }
            }
        }
 */
    }

    class BotEntryCellRenderer : JLabel(), ListCellRenderer<BotEntry> {

        init {
            isOpaque = true
        }

        override fun getListCellRendererComponent(
                list: JList<out BotEntry>, value: BotEntry, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {

            text = value.displayText
            border = EmptyBorder(1, 1, 1, 1)

            if (isSelected) {
                background = list.selectionBackground
                foreground = list.selectionForeground
            } else {
                background = list.background
                foreground = list.foreground
            }
            font = list.font

            return this
        }
    }
}