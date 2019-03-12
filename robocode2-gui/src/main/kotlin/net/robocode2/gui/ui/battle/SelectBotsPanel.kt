package net.robocode2.gui.ui.battle

import net.miginfocom.swing.MigLayout
import net.robocode2.gui.extensions.JComponentExt.addNewButton
import net.robocode2.gui.extensions.JComponentExt.addNewLabel
import net.robocode2.gui.server.Client
import net.robocode2.gui.ui.ResourceBundles.STRINGS
import net.robocode2.gui.utils.Disposable
import net.robocode2.gui.utils.Observable
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class SelectBotsPanel : JPanel(MigLayout("fill")) {

    // Private events
    private val onStartBattle = Observable<JButton>()
    private val onConnectButtonClicked = Observable<JButton>()

    private val onCancel = Observable<JButton>()
    private val onAdd = Observable<JButton>()
    private val onAddAll = Observable<JButton>()
    private val onRemove = Observable<JButton>()
    private val onRemoveAll = Observable<JButton>()

    private val serverTextField = JTextField()
    private val connectButton = JButton(connectButtonText)

    private val gameTypeComboBox = GameTypeComboBox()

    private val availableBotListModel = DefaultListModel<String>()
    private val selectedBotListModel = DefaultListModel<String>()
    private val availableBotList = JList<String>(availableBotListModel)
    private val selectedBotList = JList<String>(selectedBotListModel)

    private val connectionStatusLabel = JLabel(connectionStatus)

    private var disposables = ArrayList<Disposable>()

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

        serverTextField.text = "localhost:50000"

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

        connectButton.addActionListener { onConnectButtonClicked.notify(connectButton) }

        onConnectButtonClicked.subscribe {
            if (!Client.isConnected()) {
                Client.connect(Client.defaultUri) // FIXME: Use URI from text field + reset button to default URI
            } else {
                Client.disconnect()
            }
        }

        for (i in 1..20) {
            availableBotListModel.addElement("avail: $i")
//            selectedBotListModel.addElement("selected: $i")
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

        disposables.add(Client.onConnected.subscribe { updateConnectionState() })
        disposables.add(Client.onDisconnected.subscribe { updateConnectionState() })

        onStartBattle.subscribe { startGame() }
    }

    fun dispose() {
        disposables.forEach { it.dispose() }
        Client.disconnect()
    }

    protected fun finalize() {
        dispose()
    }

    private fun updateConnectionState() {
        connectionStatusLabel.text = connectionStatus
        connectButton.text = connectButtonText
        connectButton.revalidate()
    }

    private fun startGame() {
/*
        val gameSetup = gameTypeComboBox.gameSetup;

        val modelGameSetup: GameSetup = GameSetup(
                arenaWidth = gameSetup.height,
                arenaHeight = gameSetup.width,
                minNumberOfParticipants = gameSetup.minNumParticipants,
                maxNumberOfParticipants = gameSetup.maxNumParticipants,
                numberOfRounds = gameSetup.numberOfRounds,
                inactivityTurns = gameSetup.inactivityTurns,
                gunCoolingRate = gameSetup.gunCoolingRate
        )

        Client.startGame(modelGameSetup, HashSet())*/
    }
}
