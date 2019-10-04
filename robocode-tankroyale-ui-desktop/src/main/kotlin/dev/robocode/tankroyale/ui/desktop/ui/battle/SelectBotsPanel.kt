package dev.robocode.tankroyale.ui.desktop.ui.battle

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import net.miginfocom.swing.MigLayout
import dev.robocode.tankroyale.ui.desktop.bootstrap.BootstrapProcess
import dev.robocode.tankroyale.ui.desktop.bootstrap.BotEntry
import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewButton
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewLabel
import dev.robocode.tankroyale.ui.desktop.model.GameSetup
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles.STRINGS
import dev.robocode.tankroyale.ui.desktop.util.Event
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.EmptyBorder


@UnstableDefault
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
        get() = gameTypeComboBox.gameSetup

    init {
        val upperPanel = JPanel(MigLayout("", "[][grow][]")).apply {
            addNewLabel("game_type")
            add(gameTypeComboBox)
            addNewLabel("connection_status", "right")
            add(connectionStatusLabel, "center")
        }
        val leftSelectionPanel = JPanel(MigLayout("fill")).apply {
            add(JScrollPane(availableBotList), "grow")
            // Sets the preferred size to avoid right panel width to grow much larger than the right panel
            preferredSize = Dimension(10, 10)
            border = BorderFactory.createTitledBorder(STRINGS.get("available_bots"))
        }

        val rightSelectionPanel = JPanel(MigLayout("fill")).apply {
            add(JScrollPane(selectedBotList), "grow")
            preferredSize = Dimension(10, 10)
            border = BorderFactory.createTitledBorder(STRINGS.get("selected_bots"))
        }

        val addPanel = JPanel(MigLayout("insets 0, fill", "[fill]"))
        val removePanel = JPanel(MigLayout("insets 0, fill", "[fill]"))

        val middlePanel = JPanel(MigLayout("fill"))

        val centerSelectionPanel = JPanel(MigLayout("insets 0")).apply {
            add(addPanel, "north")
            add(middlePanel, "h 300")
            add(removePanel, "south")
        }
        val selectionPanel = JPanel(MigLayout("", "[grow][][grow]")).apply {
            add(leftSelectionPanel, "grow")
            add(centerSelectionPanel, "")
            add(rightSelectionPanel, "grow")
        }
        val buttonPanel = JPanel(MigLayout("center, insets 0"))

        val lowerPanel = JPanel(MigLayout("insets 10, fill")).apply {
            add(selectionPanel, "north")
            add(buttonPanel, "center")
        }

        add(upperPanel, "north")
        add(lowerPanel, "south, h 1000000")

        addPanel.apply {
            addNewButton("add_arrow", onAdd, "cell 0 1")
            addNewButton("add_all_arrow", onAddAll, "cell 0 2")
        }
        removePanel.apply {
            addNewButton("arrow_remove", onRemove, "cell 0 3")
            addNewButton("arrow_remove_all", onRemoveAll, "cell 0 4")
        }
        buttonPanel.apply {
            addNewButton("start_battle", onStartBattle, "tag ok")
            addNewButton("cancel", onCancel, "tag cancel")
        }

        availableBotList.cellRenderer = BotEntryCellRenderer()
        selectedBotList.cellRenderer = BotEntryCellRenderer()

        onConnectButtonClicked.subscribe {
            if (Client.isConnected) {
                Client.close()
            } else {
                Client.connect(ServerSettings.defaultUrl)
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
    }

    class BotEntryCellRenderer : JLabel(), ListCellRenderer<BotEntry> {

        init {
            isOpaque = true
        }

        override fun getListCellRendererComponent(
            list: JList<out BotEntry>, value: BotEntry, index: Int, isSelected: Boolean, cellHasFocus: Boolean
        ): Component {

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