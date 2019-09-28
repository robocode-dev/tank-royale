package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewButton
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewLabel
import dev.robocode.tankroyale.ui.desktop.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles.MESSAGES
import dev.robocode.tankroyale.ui.desktop.util.Event
import kotlinx.serialization.ImplicitReflectionSerializer
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.io.Closeable
import javax.swing.*

@ImplicitReflectionSerializer
object SelectServerDialog : JDialog(MainWindow, getWindowTitle()) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(500, 150)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(SelectServerPanel)

        onClosing {
            Client.close()
        }
    }
}

private fun getWindowTitle(): String {
    return ResourceBundles.UI_TITLES.get("select_server_dialog")
}

@ImplicitReflectionSerializer
private object SelectServerPanel : JPanel(MigLayout("fill")) {

    // Private events
    private val onAdd = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onTest = Event<JButton>()

    private val onOk = Event<JButton>()
    private val onCancel = Event<JButton>()

    private val onStartLocalServerCheckBoxChanged = Event<JCheckBox>()

    private val endpointsComboBox = JComboBox(arrayOf("localhost:55000"))
    private val addButton = addNewButton("add", onAdd)
    private val removeButton = addNewButton("remove", onRemove)
    private val testButton = addNewButton("server_test", onTest)

    init {
        val upperPanel = JPanel(MigLayout("", "[][grow][][][]")).apply {
            addNewLabel("endpoint")
            add(endpointsComboBox, "span 2, grow")
            add(addButton)
            add(removeButton)
            add(testButton, "wrap")
        }
        val lowerPanel = JPanel(MigLayout("", "[grow]"))

        add(upperPanel, "north")
        add(lowerPanel, "south")

        val okButton: JButton

        val buttonPanel = JPanel(MigLayout()).apply {
            okButton = addNewButton("ok", onOk, "tag ok")
            addNewButton("cancel", onCancel, "tag cancel")
        }
        SelectServerDialog.rootPane.defaultButton = okButton

        lowerPanel.add(buttonPanel, "center")

        NewEndpointDialog.onComplete.subscribe {
            endpointsComboBox.addItem(NewEndpointDialog.newEndpoint)

            removeButton.isEnabled = true
            okButton.isEnabled = true
            testButton.isEnabled = true
        }

        onAdd.subscribe {
            NewEndpointDialog.isVisible = true
        }

        onRemove.subscribe {
            endpointsComboBox.removeItem(endpointsComboBox.selectedItem)
            if (endpointsComboBox.itemCount == 0) {
                removeButton.isEnabled = false
                okButton.isEnabled = false
                testButton.isEnabled = false
            }
        }

        onTest.subscribe { testServerConnection() }

        onOk.subscribe {
            saveServerConfig()
            SelectServerDialog.dispose()
        }
        onCancel.subscribe {
            setFieldsToServerConfig()
            SelectServerDialog.dispose()
        }

        setFieldsToServerConfig()
    }

    private fun testServerConnection() {
        val disposables = ArrayList<Closeable>()

        disposables += Client.onConnected.subscribe {
            JOptionPane.showMessageDialog(
                this,
                MESSAGES.get("connected_successfully_to_server")
            )

            Client.close()

            disposables.forEach { it.close() }
            disposables.clear()
        }

        disposables += Client.onError.subscribe {
            val option = JOptionPane.showConfirmDialog(
                this,
                ResourceBundles.MESSAGES.get("could_not_connect_start_local_server_question"),
                ResourceBundles.MESSAGES.get("title_question"),
                JOptionPane.YES_NO_OPTION
            )
            if (option == JOptionPane.YES_OPTION) {
                ServerProcess.start()
                Client.connect(ServerSettings.endpoint)
            }

            disposables.forEach { it.close() }
            disposables.clear()
        }

        var endpoint = endpointsComboBox.selectedItem as String
        if (!endpoint.contains("//:")) { // FIXME: Use WsEndpoint
            endpoint = "ws://$endpoint"
        }

        Client.connect(endpoint) // Connect or re-connect
    }

    private fun setFieldsToServerConfig() {
        endpointsComboBox.removeAllItems()

        if (ServerSettings.userEndpoints.isNotEmpty()) {
            ServerSettings.userEndpoints.forEach { endpointsComboBox.addItem(it) }
        } else {
            endpointsComboBox.addItem(ServerSettings.endpoint)
        }
        endpointsComboBox.selectedItem = ServerSettings.endpoint
    }

    private fun saveServerConfig() {
        ServerSettings.endpoint = endpointsComboBox.selectedItem as String

        val userEndpoints = ArrayList<String>()
        val size = endpointsComboBox.itemCount
        for (i in 0 until size) {
            userEndpoints.add(endpointsComboBox.getItemAt(i))
        }
        ServerSettings.userEndpoints = userEndpoints

        ServerSettings.save()
    }
}

@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        SelectServerDialog.isVisible = true
    }
}