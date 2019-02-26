package net.robocode2.gui.ui.battle

import net.miginfocom.swing.MigLayout
import net.robocode2.gui.extensions.JComponentExt.addNewButton
import net.robocode2.gui.extensions.JComponentExt.addNewLabel
import net.robocode2.gui.server.Server
import net.robocode2.gui.ui.ResourceBundles.STRINGS
import net.robocode2.gui.utils.Disposable
import net.robocode2.gui.utils.Observable
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class SelectBotsPanel : JPanel(MigLayout("fill")) {

    // Private events
    private val onAdd = Observable<JButton>()
    private val onAddAll = Observable<JButton>()
    private val onRemove = Observable<JButton>()
    private val onRemoveAll = Observable<JButton>()
    private val onConnectButtonClicked = Observable<JButton>()

    private val serverTextField = JTextField()
    private val connectButton = JButton(connectButtonText)

    private val availableBotListModel = DefaultListModel<String>()
    private val selectedBotListModel = DefaultListModel<String>()
    private val availableBotList = JList<String>(availableBotListModel)
    private val selectedBotList = JList<String>(selectedBotListModel)

    private val connectionStatusLabel = JLabel(connectionStatus)

    private var disposables = ArrayList<Disposable>()

    private val connectionStatus: String
        get() =
            if (Server.isConnected()) {
                STRINGS.get("connected")
            } else {
                STRINGS.get("disconnected")
            }

    private val connectButtonText: String
        get() =
            if (Server.isConnected()) {
                STRINGS.get("disconnect")
            } else {
                STRINGS.get("connect")
            }

    init {
        val upperPanel = JPanel(MigLayout("fill", "[][grow][]"))
        val lowerPanel = JPanel(MigLayout("insets 10, fill", "[grow][][grow]"))

        add(upperPanel, "north")
        add(lowerPanel, "south")

        upperPanel.addNewLabel("server_endpoint")
        upperPanel.add(serverTextField, "span 2, grow")
        upperPanel.add(connectButton, "wrap")

        serverTextField.text = "localhost:50000"

        val gameTypeComboBox = JComboBox(listOf("one", "two", "three").toTypedArray())
        upperPanel.addNewLabel("game_type")
        upperPanel.add(gameTypeComboBox)
        upperPanel.addNewLabel("connection_status", "right")
        upperPanel.add(connectionStatusLabel, "center")

        val leftPanel = JPanel(MigLayout("fill"))
        leftPanel.add(JScrollPane(availableBotList), "grow")

        val rightPanel = JPanel(MigLayout("fill"))
        rightPanel.add(JScrollPane(selectedBotList), "grow")

        val centerPanel = JPanel(MigLayout("insets 0"))

        // Sets the preferred size to avoid right panel with to grow much larger than the right panel
        leftPanel.preferredSize = Dimension(10, 10)
        rightPanel.preferredSize = Dimension(10, 10)

        lowerPanel.add(leftPanel, "grow")
        lowerPanel.add(centerPanel, "")
        lowerPanel.add(rightPanel, "grow")

        leftPanel.border = BorderFactory.createTitledBorder(STRINGS.get("available_bots"))
        rightPanel.border = BorderFactory.createTitledBorder(STRINGS.get("selected_bots"))

        val addPanel = JPanel(MigLayout("insets 0, fill", "[fill]"))
        val removePanel = JPanel(MigLayout("insets 0, fill", "[fill]"))

        val middlePanel = JPanel(MigLayout("fill"))

        centerPanel.add(addPanel, "north")
        centerPanel.add(middlePanel, "h 300")
        centerPanel.add(removePanel, "south")

        addPanel.addNewButton("add_arrow", onAdd, "cell 0 1")
        addPanel.addNewButton("add_all_arrow", onAddAll, "cell 0 2")
        removePanel.addNewButton("arrow_remove", onRemove, "cell 0 3")
        removePanel.addNewButton("arrow_remove_all", onRemoveAll, "cell 0 4")

        connectButton.addActionListener { onConnectButtonClicked.notify(connectButton) }

        onConnectButtonClicked.subscribe {
            if (!Server.isConnected()) {
                Server.connect(Server.defaultUri) // FIXME: Use URI from text field + reset button to default URI
            } else {
                Server.disconnect()
            }
        }

        for (i in 1..20) {
            availableBotListModel.addElement("avail: $i")
//            selectedBotListModel.addElement("selected: $i")
        }

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

        disposables.add(Server.onConnected.subscribe { updateConnectionState() })
        disposables.add(Server.onDisconnected.subscribe { updateConnectionState() })
    }

    fun dispose() {
        disposables.forEach { it.dispose() }
    }

    protected fun finalize() {
        dispose()
    }

    private fun updateConnectionState() {
        connectionStatusLabel.text = connectionStatus
        connectButton.text = connectButtonText
        connectButton.revalidate()
    }
}
