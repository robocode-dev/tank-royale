package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.PortInputField
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.components.RcToolTip
import dev.robocode.tankroyale.gui.ui.components.SwitchButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.createAddButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.createButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.createCancelButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.createEditButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.createOkButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.createRemoveButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.enableAll
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showMessage
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosed
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onOpened
import dev.robocode.tankroyale.gui.ui.server.RemoteServer
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.MessageDialog
import net.miginfocom.swing.MigLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionListener
import javax.swing.*
import java.awt.event.ItemEvent
import java.awt.event.ItemListener

class ServerConfigDialog : RcDialog(MainFrame, "server_config_dialog") {
    init {
        contentPane.add(ServerConfigPanel(this))
        pack()
        setLocationRelativeTo(owner) // center on owner window

        onOpened {
            ServerSettings.backup()
        }
    }
}

private class ServerConfigPanel(val owner: RcDialog) : JPanel() {

    val onToggleRemoteServer = Event<Boolean>()
    val onPortUpdated = Event<Short>()
    val onOk = Event<JButton>()
    val onCancel = Event<JButton>()
    val onTest = Event<JButton>()
    val onAdd = Event<JButton>()
    val onEdit = Event<JButton>()
    val onRemove = Event<JButton>()

    private val okButton = createOkButton(onOk)
    private val cancelButton = createCancelButton(onCancel)
    private val addButton = createAddButton(onAdd)
    private val editButton = createEditButton(onEdit)
    private val removeButton = createRemoveButton(onRemove)
    private val testButton = createButton("test", onTest)

    private val selectedServerLabel = createSelectedServerLabel()
    private val serverSwitchButton = createServerSwitchButton()
    private val useRemoteOrLocalServerLabel = createUseRemoteOrLocalServerLabel()
    private val localPortInputField = createLocalPortInputField()
    private val remoteServerComboBox = createRemoteServerComboBox()
    private val localServerPanel = createLocalServerPanel()
    private val remoteServerPanel = createRemoteServerPanel()

    init {
        setupLayout()
        setupEventHandlers()
        toggleRemoteServer(ServerSettings.useRemoteServer)
        addToolTipTextOnButtons()
    }

    private fun setupLayout() {
        layout = MigLayout("insets 10, fillx", "[grow]", "[]10[]10[]20[]")
        add(createUpperPanel(), "wrap")
        add(localServerPanel, "growx, wrap")
        add(remoteServerPanel, "growx, wrap")
        add(createButtonPanel(), "growx")
    }

    private fun setupEventHandlers() {
        onToggleRemoteServer.subscribe(this) { isSelected ->
            toggleRemoteServer(isSelected)
        }

        onPortUpdated.subscribe(this) { port ->
            ServerSettings.localPort = port
        }

        onAdd.subscribe(this) { addRemoteServer() }

        onEdit.subscribe(this) { editRemoteServer() }

        onRemove.subscribe(this) { removeRemoteServer() }

        onTest.subscribe(this) { testServerConnection() }

        onOk.subscribe(this) {
            dispose()
        }

        onCancel.subscribe(this) {
            dispose()
            ServerSettings.restore()
        }
    }

    private fun dispose() {
        owner.dispose()
    }

    private fun createSelectedServerLabel() =
        object: JLabel(ServerSettings.serverUrl()) {
            override fun createToolTip() = RcToolTip()
        }.apply {
            font = Font(font.family, Font.BOLD, font.size)
            foreground = Color(0x00, 0x7f, 0x00)

            toolTipText = Messages.get("selected_server_is_used")
        }

    private fun createServerSwitchButton() = SwitchButton(ServerSettings.useRemoteServer).apply {
        addSwitchHandler { isSelected -> onToggleRemoteServer.fire(isSelected) }

        toolTipText = Messages.get("switch_between_local_and_remote_server")
    }

    private fun createUseRemoteOrLocalServerLabel() = JLabel().apply {
        text = getUseRemoteOrLocalServerText(serverSwitchButton.isSelected)
    }

    private fun createLocalPortInputField() = PortInputField(ServerSettings.localPort).apply {
        addPortUpdatedHandler { port -> onPortUpdated.fire(port) }

        toolTipText = Messages.get("port_used_for_local_server")
    }

    private fun createRemoteServerComboBox() = JComboBox(getRemoteServerUrls()).apply {
        preferredSize = Dimension(200, preferredSize.height)
        selectedItem = ServerSettings.useRemoteServerUrl
        addItemListener(createRemoteServerComboBoxItemListener())
        addActionListener(createRemoteServerComboBoxActionListener())

        toolTipText = Messages.get("selected_server_is_used")
    }

    private fun createRemoteServerComboBoxItemListener() = ItemListener { itemEvent ->
        if (itemEvent.stateChange == ItemEvent.SELECTED) {
            val serverUrl = itemEvent.item as String
            ServerSettings.useRemoteServerUrl = serverUrl
            updateSelectedServerLabel()
        }
    }

    private fun createRemoteServerComboBoxActionListener() = ActionListener { actionEvent ->
        val comboBox = (actionEvent.source as JComboBox<*>)
        val hasItem = comboBox.itemCount > 0

        okButton.isEnabled = hasItem

        setRemoteServerButtonStates(comboBox)
    }

    private fun createUpperPanel() = JPanel(MigLayout("fillx", "[right][grow]", "[][]")).apply {
        addLabel("option.server.selected_server")
        add(selectedServerLabel, "growx, wrap")
        addLabel("option.server.use_remote_server")
        add(createSwitchPanel(), "wrap")
    }

    private fun createSwitchPanel() = JPanel().apply {
        add(serverSwitchButton)
        add(useRemoteOrLocalServerLabel)
    }

    private fun createLocalServerPanel() = JPanel(MigLayout("insets 10, fillx", "", "[][]")).apply {
        border = BorderFactory.createTitledBorder(Strings.get("option.server.local_server"))
        add(createPortPanel(), "wrap")
    }

    private fun createPortPanel() = JPanel().apply {
        addLabel("port")
        add(localPortInputField, "wrap")
    }

    private fun createRemoteServerPanel() = JPanel(MigLayout("insets 10, fillx", "[right][grow][]", "[][]")).apply {
        border = BorderFactory.createTitledBorder(Strings.get("option.server.remote_server"))
        addLabel("server")
        add(remoteServerComboBox, "growx")
        add(testButton, "wrap")

        val buttonPanel = JPanel(MigLayout("insets 0, left")).apply {
            add(addButton)
            add(editButton)
            add(removeButton)
        }
        add(buttonPanel, "skip 1, split 2")
    }

    private fun createButtonPanel() = JPanel(MigLayout("insets 0, center")).apply {
        add(okButton, "split 2")
        add(cancelButton)
    }

    private fun getRemoteServerUrls(): Array<String> = ServerSettings.remoteServerUrls.toTypedArray()

    private fun toggleRemoteServer(useRemoteServer: Boolean) {
        ServerSettings.useRemoteServer = useRemoteServer

        updateSelectedServerLabel()
        updateRemoteServerComboBox()

        useRemoteOrLocalServerLabel.text = getUseRemoteOrLocalServerText(useRemoteServer)

        val hasItems = remoteServerComboBox.itemCount > 0

        okButton.isEnabled = !useRemoteServer || hasItems

        setRemoteServerButtonStates(remoteServerComboBox)

        localServerPanel.enableAll(!useRemoteServer)
        enableRemoteServerPanel(useRemoteServer)
    }

    private fun enableRemoteServerPanel(enable: Boolean) {
        remoteServerPanel.enableAll(enable)

        setRemoteServerButtonStates(remoteServerComboBox)
    }

    private fun addToolTipTextOnButtons() {
        addButton.toolTipText = Messages.get("add_remote_server")
        editButton.toolTipText = Messages.get("edit_remote_server")
        removeButton.toolTipText = Messages.get("remove_remote_server")
        testButton.toolTipText = Messages.get("test_remote_server")
    }

    private fun getUseRemoteOrLocalServerText(useRemoteServer: Boolean): String =
        Strings.get(if (useRemoteServer) "option.server.remove_server_is_used" else "option.server.local_server_is_used")

    private fun updateSelectedServerLabel() {
        selectedServerLabel.text = ServerSettings.serverUrl()
    }

    private fun addRemoteServer() {
        AddRemoteServerDialog(owner).apply {
            onClosed { updateRemoteServerComboBox() }
            isVisible = true
        }
    }

    private fun editRemoteServer() {
        val serverUrl = selectedServerUrl()
        if (serverUrl == null) {
            addRemoteServer()
        } else {
            EditRemoteServerDialog(serverUrl, owner).apply {
                onClosed { updateRemoteServerComboBox() }
                isVisible = true
            }
        }
    }

    private fun removeRemoteServer() {
        val selectedServerUrl: String? = selectedServerUrl()

        if (!MessageDialog.showConfirm(String.format(Messages.get("confirm_remove"), selectedServerUrl))) return

        remoteServerComboBox.removeItem(selectedServerUrl)

        selectedServerUrl?.let {
            ServerSettings.removeRemoteServer(selectedServerUrl)

            if (remoteServerComboBox.itemCount > 0) {
                remoteServerComboBox.selectedIndex = 0
                ServerSettings.useRemoteServerUrl = selectedServerUrl() ?: ""
            } else {
                ServerSettings.useRemoteServerUrl = ""
            }
        }
    }

    private fun testServerConnection() {
        val serverUrl = selectedServerUrl()
        val messageKey = if (serverUrl != null && RemoteServer.isRunning(serverUrl)) "server_is_running" else "server_not_found"
        val message = String.format(Messages.get(messageKey), serverUrl)
        showMessage(message)
    }

    private fun selectedServerUrl() = (remoteServerComboBox.selectedItem as String?)?.trim()

    private fun updateRemoteServerComboBox() {
        val selectedServerUrl = selectedServerUrl() // store selected item

        val selectedIndex = remoteServerComboBox.selectedIndex // store selected index as fallback

        remoteServerComboBox.removeAllItems()
        ServerSettings.remoteServerUrls.forEach { removeServerUrl ->
            remoteServerComboBox.addItem(removeServerUrl)
        }

        if (selectedServerUrl == null && ServerSettings.remoteServerUrls.size > 0) {
            remoteServerComboBox.selectedIndex = 0
        } else {
            remoteServerComboBox.selectedIndex = selectedIndex // restore selected index
            remoteServerComboBox.selectedItem = selectedServerUrl // restore selected item (overrides index, if item found)
        }
        setRemoteServerButtonStates(remoteServerComboBox)
    }

    fun setRemoteServerButtonStates(comboBox: JComboBox<*>) {
        if (!remoteServerPanel.isEnabled) {
            editButton.isEnabled = false
            removeButton.isEnabled = false
            testButton.isEnabled = false
        } else {
            val hasItem = comboBox.itemCount > 0
            editButton.isEnabled = hasItem
            removeButton.isEnabled = hasItem
            testButton.isEnabled = hasItem
        }
    }
}
