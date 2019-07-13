package net.robocode2.gui.ui.server

import kotlinx.serialization.ImplicitReflectionSerializer
import net.miginfocom.swing.MigLayout
import net.robocode2.gui.client.Client
import net.robocode2.gui.extensions.JComponentExt.addNewButton
import net.robocode2.gui.extensions.JComponentExt.addNewLabel
import net.robocode2.gui.extensions.WindowExt.onClosing
import net.robocode2.gui.settings.ServerSettings
import net.robocode2.gui.ui.MainWindow
import net.robocode2.gui.ui.ResourceBundles
import net.robocode2.gui.ui.ResourceBundles.MESSAGES
import net.robocode2.gui.ui.ResourceBundles.STRINGS
import net.robocode2.gui.utils.Disposable
import net.robocode2.gui.utils.Event
import java.awt.Cursor
import java.awt.Dimension
import java.awt.EventQueue
import java.net.URI
import java.net.URISyntaxException
import javax.swing.*
import net.robocode2.gui.extensions.JTextFieldExt.setInputVerifier

@ImplicitReflectionSerializer
object ServerConfigDialog : JDialog(MainWindow, getWindowTitle()) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(500, 150)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(ServerConfigPanel)

        onClosing {
            Client.close()
        }
    }
}

private fun getWindowTitle(): String {
    return ResourceBundles.UI_TITLES.get("server_config_dialog")
}

@ImplicitReflectionSerializer
private object ServerConfigPanel : JPanel(MigLayout("fill")) {

    // Private events
    private val onOk = Event<JButton>()
    private val onCancel = Event<JButton>()
    private val onResetServerConfig = Event<JButton>()

    private val onTestButtonClicked = Event<JButton>()
    private val onRemoteServerCheckBoxChanged = Event<JCheckBox>()

    private val addressTextField = JTextField()
    private val portTextField = JTextField("${ServerSettings.port}", 5)
    private val testButton = JButton(STRINGS.get("server_test"))

    private val remoteServerCheckBox = JCheckBox(STRINGS.get("use_remote_server"),
            ServerSettings.useRemoteServer)

    init {
        val upperPanel = JPanel(MigLayout("", "[][grow][]"))
        val lowerPanel = JPanel(MigLayout("", "[grow]"))
        add(upperPanel, "north")
        add(lowerPanel, "south")

        portTextField.setInputVerifier { portTextFieldVerifier() }

        upperPanel.addNewLabel("server_endpoint")
        upperPanel.add(addressTextField, "span 2, grow")
        upperPanel.add(portTextField)
        upperPanel.add(testButton, "wrap")

        addressTextField.text = ServerSettings.address
        addressTextField.isEnabled = remoteServerCheckBox.isSelected

        upperPanel.add(remoteServerCheckBox)

        val buttonPanel = JPanel(MigLayout())
        lowerPanel.add(buttonPanel, "center")

        buttonPanel.addNewButton("ok", onOk, "tag ok")
        buttonPanel.addNewButton("cancel", onCancel, "tag cancel")
        buttonPanel.addNewButton("reset_server_config_to_default", onResetServerConfig, "tag apply")

        testButton.addActionListener { onTestButtonClicked.publish(testButton) }
        remoteServerCheckBox.addActionListener { onRemoteServerCheckBoxChanged.publish(remoteServerCheckBox) }

        onRemoteServerCheckBoxChanged.subscribe { addressTextField.isEnabled = remoteServerCheckBox.isSelected }

        onTestButtonClicked.subscribe { testServerConnection() }

        onOk.subscribe {
            if (saveServerConfig())
                ServerConfigDialog.dispose()
        }
        onCancel.subscribe {
            setFieldsToServerConfig()
        }

        onCancel.subscribe { ServerConfigDialog.dispose() }
        onResetServerConfig.subscribe { resetServerConfig() }
    }

    private fun testServerConnection() {
        val disposables = ArrayList<Disposable>()

        disposables += Client.onConnected.subscribe {
            JOptionPane.showMessageDialog(this,
                    MESSAGES.get("connected_successfully_to_server"))

            Client.close()

            disposables.forEach { it.dispose() }
            disposables.clear()
        }

        disposables += Client.onDisconnected.subscribe {
            cursor = Cursor.getDefaultCursor()
        }

        disposables += Client.onError.subscribe {
            JOptionPane.showMessageDialog(this,
                    MESSAGES.get("could_not_connect_to_server"),
                    MESSAGES.get("title_warning"),
                    JOptionPane.WARNING_MESSAGE)

            cursor = Cursor.getDefaultCursor()

            disposables.forEach { it.dispose() }
            disposables.clear()
        }

        val endpoint = "ws://${addressTextField.text}:${portTextField.text}"

        Client.connect(endpoint) // Connect or re-connect
    }

    private fun resetServerConfig() {
        ServerSettings.resetToDefault()
        setFieldsToServerConfig()
    }

    private fun setFieldsToServerConfig() {
        addressTextField.text = ServerSettings.address
        portTextField.text = "${ServerSettings.port}"
        remoteServerCheckBox.isSelected = ServerSettings.useRemoteServer
    }

    private fun saveServerConfig(): Boolean {
        try {
            URI(addressTextField.text)

            ServerSettings.address = addressTextField.text
            ServerSettings.port = portTextField.text.toUShort()
            ServerSettings.useRemoteServer = remoteServerCheckBox.isSelected
            ServerSettings.save()
            return true

        } catch (ex: URISyntaxException) {
            JOptionPane.showMessageDialog(null,
                    MESSAGES.get("endpoint_is_not_valid"),
                    MESSAGES.get("title_error"),
                    JOptionPane.ERROR_MESSAGE)
            return false
        }
    }

    private fun portTextFieldVerifier(): Boolean {
        val port: UShort? = try {
            portTextField.text.trim().toUShort()
        } catch (e: NumberFormatException) {
            null
        }
        val valid = port != null && port >= 1024u && port <= 65535u
        if (!valid || port == null) {
            JOptionPane.showMessageDialog(this, MESSAGES.get("port_number_range"))
            portTextField.text = "${ServerSettings.port}"
        }
        return valid
    }
}

@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        ServerConfigDialog.isVisible = true
    }
}