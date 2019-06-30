package net.robocode2.gui.ui.server

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
import net.robocode2.gui.utils.Event
import java.awt.Cursor
import java.awt.Dimension
import java.awt.EventQueue
import java.net.URI
import javax.swing.*

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

private object ServerConfigPanel : JPanel(MigLayout("fill")) {

    // Private events
    private val onOk = Event<JButton>()
    private val onCancel = Event<JButton>()
    private val onResetServerConfig = Event<JButton>()

    private val onTestButtonClicked = Event<JButton>()

    private val serverTextField = JTextField()
    private val testButton = JButton(testButtonText)

    private val testButtonText: String
        get() = STRINGS.get("server_test")

    private val remoteServerCheckbox = JCheckBox(STRINGS.get(("use_remote_server")),
            ServerSettings.useRemoteServer)

    init {
//        Client.connect(URI(serverEndpoint))

        val upperPanel = JPanel(MigLayout("", "[][grow][]"))
        val lowerPanel = JPanel(MigLayout("", "[grow]"))
        add(upperPanel, "north")
        add(lowerPanel, "south")

        upperPanel.addNewLabel("server_endpoint")
        upperPanel.add(serverTextField, "span 2, grow")
        upperPanel.add(testButton, "wrap")

        serverTextField.text = ServerSettings.endpoint

        upperPanel.add(remoteServerCheckbox)

        val buttonPanel = JPanel(MigLayout())
        lowerPanel.add(buttonPanel, "center")

        buttonPanel.addNewButton("ok", onOk, "tag ok")
        buttonPanel.addNewButton("cancel", onCancel, "tag cancel")
        buttonPanel.addNewButton("reset_server_config_to_default", onResetServerConfig, "tag apply")

        testButton.addActionListener { onTestButtonClicked.publish(testButton) }

        onTestButtonClicked.subscribe {
            cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
            Client.connect(URI(serverTextField.text))
        }

        Client.onConnected.subscribe {
            JOptionPane.showMessageDialog(null,
                    MESSAGES.get("connected_successfully_to_server"))

            Client.close()

            cursor = Cursor.getDefaultCursor()
        }

        Client.onDisconnected.subscribe {
            cursor = Cursor.getDefaultCursor()
        }

        Client.onError.subscribe {
            JOptionPane.showMessageDialog(null,
                    MESSAGES.get("could_not_connect_to_server"),
                    MESSAGES.get("title_warning"),
                    JOptionPane.WARNING_MESSAGE)

            cursor = Cursor.getDefaultCursor()
        }

        onOk.subscribe { ServerSettings.save(); ServerConfigDialog.dispose() }
        onCancel.subscribe { ServerConfigDialog.dispose() }
        onResetServerConfig.subscribe { resetServerConfig() }
    }

    private fun resetServerConfig() {
        ServerSettings.resetToDefault()

        serverTextField.text = ServerSettings.endpoint
        remoteServerCheckbox.isSelected = ServerSettings.useRemoteServer
    }
}

private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        ServerConfigDialog.isVisible = true
    }
}