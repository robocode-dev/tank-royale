package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.PortInputField
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.components.SwitchButton
import net.miginfocom.swing.MigLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.JPanel
import javax.swing.*
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addCancelButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.enableAll
import dev.robocode.tankroyale.gui.util.Event


object ServerConfigDialog : RcDialog(MainFrame, "server_config_dialog") {

    init {
        contentPane.add(ServerConfigPanel)
        pack()
        setLocationRelativeTo(MainFrame) // center on main window
    }
}

object ServerConfigPanel : JPanel() {

    val onToggleRemoteServer = Event<Boolean>()
    val onPortUpdated = Event<Short>()

    val onOk = Event<JButton>()
    val onCancel = Event<JButton>()
    val onTest = Event<JButton>()
    val onAdd = Event<JButton>()
    val onRemove = Event<JButton>()

    val selectedServerLabel = JLabel(ServerSettings.useRemoteServerUrl).apply {
        font = Font(font.family, Font.BOLD, font.size)
        foreground = Color(0x00, 0x7f, 0x00)
    }

    val remoteServerComboBox = JComboBox(getRemoteServerUrls()).apply {
        preferredSize = Dimension(150, preferredSize.height)
    }

    val serverSwitchButton = SwitchButton(true).apply {
        addSwitchHandler { isSelected -> onToggleRemoteServer.fire(isSelected) }
    }

    val useRemoveOrLocalServerLabel = JLabel().apply {
        text = getUseRemoveOrLocalServerText(serverSwitchButton.isSelected)
    }

    val localPortInputField = PortInputField().apply {
        addPortUpdatedHandler { port -> onPortUpdated.fire(port) }
    }

    val localServerPanel: JPanel
    var remoteServerPanel: JPanel

    init {
        var okButton: JButton? = null

        setLayout(MigLayout("insets 10, fillx", "[grow]", "[]10[]10[]10[]"))

        // Selected server
        addLabel("selected_server", "split 2")
        add(selectedServerLabel, "growx, wrap")

        addLabel("use_remote_server", "split 3")

        add(serverSwitchButton)
        add(useRemoveOrLocalServerLabel, "left, wrap")

        // Local server group
        localServerPanel = JPanel(MigLayout("insets 10, fillx", "[right][grow]", "[][]")).apply {
            setBorder(BorderFactory.createTitledBorder(Strings.get("local_server")))
            addLabel("port")
            add(localPortInputField, "wrap")
//            add(JCheckBox("Secure server (WSS)"), "span 2")
        }
        add(localServerPanel, "growx, wrap")

        // Remote server group
        remoteServerPanel = JPanel(MigLayout("insets 10, fillx", "[right][grow][]", "[][]")).apply {
            setBorder(BorderFactory.createTitledBorder(Strings.get("remote_server")))
            addLabel("server")
            add(remoteServerComboBox, "growx")

            addButton("test", onTest, "wrap")
            addButton("add", onAdd, "skip 1, split 2")
            addButton("remove", onRemove)
        }
        add(remoteServerPanel, "growx, wrap")

        // OK and Cancel buttons
        val buttonPanel = JPanel(MigLayout("insets 0, center")).apply {
            okButton = addOkButton(onOk, "split 2")
            addCancelButton(onCancel)
        }
        add(buttonPanel, "growx")

        onToggleRemoteServer.subscribe(this) { isSelected ->
            toggleRemoteServer(isSelected)
        }

        onPortUpdated.subscribe(this) { port ->
            ServerSettings.localPort = port
        }

        val useRemoteServer = !ServerSettings.useRemoteServerUrl.isBlank()
        toggleRemoteServer(useRemoteServer)
    }

    private fun getRemoteServerUrls(): Array<String> {
        return ServerSettings.remoteServerUrls.toTypedArray()
    }

    private fun toggleRemoteServer(useRemoteServer: Boolean) {
        remoteServerPanel.enableAll(useRemoteServer)
        localServerPanel.enableAll(!useRemoteServer)

        useRemoveOrLocalServerLabel.text = getUseRemoveOrLocalServerText(useRemoteServer)
    }

    private fun getUseRemoveOrLocalServerText(useRemoteServer: Boolean) =
        Strings.get(if (useRemoteServer) "remove_server_is_used" else "local_server_is_used")
}


fun main() {
    ServerConfigDialog.isVisible = true
}