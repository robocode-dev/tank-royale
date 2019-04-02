package net.robocode2.gui.ui.battle

import net.miginfocom.swing.MigLayout
import net.robocode2.gui.client.Client
import net.robocode2.gui.extensions.JComponentExt.addNewButton
import net.robocode2.gui.extensions.JComponentExt.addNewLabel
import net.robocode2.gui.model.BotAddress
import net.robocode2.gui.model.BotInfo
import net.robocode2.gui.ui.ResourceBundles.STRINGS
import net.robocode2.gui.utils.Event
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.EmptyBorder


class SelectBotsPanel : JPanel(MigLayout("fill")) {

    // Private events
    private val onStartBattle = Event<JButton>()
    private val onConnectButtonClicked = Event<JButton>()

    private val onCancel = Event<JButton>()
    private val onAdd = Event<JButton>()
    private val onAddAll = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onRemoveAll = Event<JButton>()

    private val serverTextField = JTextField()
    private val connectButton = JButton(connectButtonText)

    private val gameTypeComboBox = GameTypeComboBox()

    private val availableBotListModel = DefaultListModel<BotInfo>()
    private val selectedBotListModel = DefaultListModel<BotInfo>()
    private val availableBotList = JList<BotInfo>(availableBotListModel)
    private val selectedBotList = JList<BotInfo>(selectedBotListModel)

    private val connectionStatusLabel = JLabel(connectionStatus)

    private val connectionStatus: String
        get() = STRINGS.get(if (Client.isConnected()) "connected" else "disconnected")

    private val connectButtonText: String
        get() = STRINGS.get(if (Client.isConnected()) "disconnect" else "connect")

    init {
        val upperPanel = JPanel(MigLayout("", "[][grow][]"))
        val lowerPanel = JPanel(MigLayout("insets 10, fill"))

        add(upperPanel, "north")
        add(lowerPanel, "south, h 1000000")

        upperPanel.addNewLabel("server_endpoint")
        upperPanel.add(serverTextField, "span 2, grow")
        upperPanel.add(connectButton, "wrap")

        serverTextField.text = "localhost:55000"

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

        availableBotList.cellRenderer = BotInfoCellRenderer()
        selectedBotList.cellRenderer = BotInfoCellRenderer()

        connectButton.addActionListener { onConnectButtonClicked.publish(connectButton) }

        onConnectButtonClicked.subscribe {
            if (Client.isConnected()) {
                Client.close()
            } else {
                Client.connect(Client.defaultUri) // FIXME: Use URI from text field + reset button to default URI
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
        Client.onDisconnected.subscribe {
            updateConnectionState()
            availableBotListModel.clear()
            selectedBotListModel.clear()
        }
        Client.onBotListUpdate.subscribe { updateBotList() }

        onStartBattle.subscribe { startGame() }
    }

    private fun updateConnectionState() {
        connectionStatusLabel.text = connectionStatus
        connectButton.text = connectButtonText
        connectButton.revalidate()
    }

    private fun updateBotList() {
        availableBotListModel.clear()
        Client.getAvailableBots().forEach {
            availableBotListModel.addElement(it)
        }
    }

    private fun startGame() {
        val selectedBotAddresses = HashSet<BotAddress>()

        selectedBotListModel.elements().toList().forEach {
            selectedBotAddresses.add(it.botAddress)
        }
        Client.onGameStarted.subscribe { BattleDialog.dispose() }

        Client.startGame(gameTypeComboBox.gameSetup, selectedBotAddresses)
    }

    inner class BotInfoCellRenderer : JLabel(), ListCellRenderer<BotInfo> {

        init {
            isOpaque = true
        }

        override fun getListCellRendererComponent(
                list: JList<out BotInfo>?, value: BotInfo?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {

            text = value?.displayText
            border = EmptyBorder(1, 1, 1, 1)

            if (isSelected) {
                background = list?.selectionBackground
                foreground = list?.selectionForeground
            } else {
                background = list?.background
                foreground = list?.foreground
            }
            font = list?.font

            return this
        }
    }
}
