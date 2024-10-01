package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.model.Message
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.PortInputField
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.components.SwitchButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.enableAll
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showMessage
import dev.robocode.tankroyale.gui.ui.server.RemoteServer
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.MessageDialog
import net.miginfocom.swing.MigLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.*
import java.awt.event.ItemEvent
import java.awt.event.ItemListener

object ServerConfigDialog : RcDialog(MainFrame, "server_config_dialog") {
    init {
        contentPane.add(ServerConfigPanel())
        pack()
        setLocationRelativeTo(owner) // center on owner window
    }
}

private class ServerConfigPanel : JPanel() {

    val onToggleRemoteServer = Event<Boolean>()
    val onPortUpdated = Event<Short>()
    val onOk = Event<JButton>()
    val onCancel = Event<JButton>()
    val onTest = Event<JButton>()
    val onAdd = Event<JButton>()
    val onEdit = Event<JButton>()
    val onRemove = Event<JButton>()

    private val selectedServerLabel = createSelectedServerLabel()
    private val serverSwitchButton = createServerSwitchButton()
    private val useRemoteOrLocalServerLabel = createUseRemoteOrLocalServerLabel()
    private val localPortInputField = createLocalPortInputField()
    private val remoteServerComboBox = createRemoteServerComboBox()
    private val localServerPanel = createLocalServerPanel()
    private val remoteServerPanel = createRemoteServerPanel()

    private lateinit var okButton: JButton
    private lateinit var cancelButton: JButton
    private lateinit var addButton: JButton
    private lateinit var editButton: JButton
    private lateinit var removeButton: JButton
    private lateinit var testButton: JButton

    init {
        setupLayout()
        setupEventHandlers()
        toggleRemoteServer(ServerSettings.useRemoteServer)
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

        onRemove.subscribe(this) { removeRemoteServer() }

        onTest.subscribe(this) { testServerConnection() }
    }

    private fun createSelectedServerLabel() = JLabel(ServerSettings.serverUrl()).apply {
        font = Font(font.family, Font.BOLD, font.size)
        foreground = Color(0x00, 0x7f, 0x00)
    }

    private fun createServerSwitchButton() = SwitchButton(ServerSettings.useRemoteServer).apply {
        addSwitchHandler { isSelected -> onToggleRemoteServer.fire(isSelected) }
    }

    private fun createUseRemoteOrLocalServerLabel() = JLabel().apply {
        text = getUseRemoteOrLocalServerText(serverSwitchButton.isSelected)
    }

    private fun createLocalPortInputField() = PortInputField(ServerSettings.localPort).apply {
        addPortUpdatedHandler { port -> onPortUpdated.fire(port) }
    }

    private fun createRemoteServerComboBox() = JComboBox(getRemoteServerUrls()).apply {
        preferredSize = Dimension(200, preferredSize.height)
        selectedItem = ServerSettings.useRemoteServerUrl
        addItemListener(createRemoteServerComboBoxItemListener())
    }

    private fun createRemoteServerComboBoxItemListener() = ItemListener { itemEvent ->
        if (itemEvent.stateChange == ItemEvent.SELECTED) {
            val serverUrl = itemEvent.item as String
            ServerSettings.useRemoteServerUrl = serverUrl
            updateSelectedServerLabel()
        }
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
        testButton = addButton("test", onTest, "wrap")

        val buttonPanel = JPanel(MigLayout("insets 0, left")).apply {
            addButton = addButton("add", onAdd)
            editButton = addButton("edit", onEdit)
            removeButton = addButton("remove", onRemove)
        }
        add(buttonPanel, "skip 1, split 2")
    }

    private fun createButtonPanel() = JPanel(MigLayout("insets 0, center")).apply {
        okButton = addButton("ok", onOk, "split 2")
        cancelButton = addButton("cancel", onCancel)
    }

    private fun getRemoteServerUrls(): Array<String> = ServerSettings.remoteServerUrls.toTypedArray()

    private fun toggleRemoteServer(useRemoteServer: Boolean) {
        ServerSettings.useRemoteServer = useRemoteServer
        updateSelectedServerLabel()
        useRemoteOrLocalServerLabel.text = getUseRemoteOrLocalServerText(useRemoteServer)
        remoteServerPanel.enableAll(useRemoteServer)
        localServerPanel.enableAll(!useRemoteServer)
    }

    private fun getUseRemoteOrLocalServerText(useRemoteServer: Boolean): String =
        Strings.get(if (useRemoteServer) "option.server.remove_server_is_used" else "option.server.local_server_is_used")

    private fun updateSelectedServerLabel() {
        selectedServerLabel.text = ServerSettings.serverUrl()
    }

    private fun addRemoteServer() {
        AddRemoteServerDialog.isVisible = true
    }

    private fun removeRemoteServer() {
        val selectedServerUrl: String = remoteServerComboBox.selectedItem as String

        if (!MessageDialog.showConfirm(String.format(Messages.get("confirm_remove"), selectedServerUrl))) return

        remoteServerComboBox.removeItem(selectedServerUrl)

        ServerSettings.removeRemoteServer(selectedServerUrl)

        if (remoteServerComboBox.itemCount == 0) {
            removeButton.isEnabled = false
            okButton.isEnabled = false
            testButton.isEnabled = false
        }
    }

    private fun testServerConnection() {
        val serverUrl = remoteServerComboBox.selectedItem as String
        val messageKey = if (RemoteServer.isRunning(serverUrl)) "server_is_running" else "server_not_found"
        val message = String.format(Messages.get(messageKey), serverUrl)
        showMessage(message)
    }
}

fun main() {
    ServerConfigDialog.isVisible = true
}