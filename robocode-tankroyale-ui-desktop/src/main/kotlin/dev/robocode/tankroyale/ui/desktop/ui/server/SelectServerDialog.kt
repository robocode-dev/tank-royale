package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewButton
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewLabel
import dev.robocode.tankroyale.ui.desktop.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles.MESSAGES
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles.STRINGS
import dev.robocode.tankroyale.ui.desktop.util.Disposable
import dev.robocode.tankroyale.ui.desktop.util.Event
import kotlinx.serialization.ImplicitReflectionSerializer
import net.miginfocom.swing.MigLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.*
import javax.swing.text.JTextComponent

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

    private val startLocalServerCheckBox = JCheckBox(STRINGS.get("start_local_server"), ServerSettings.startLocalServer)

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

//        endpointsComboBox.isEditable = true
//
//        // Set caret position to avoid text getting selected
//        (endpointsComboBox.editor.editorComponent as JTextComponent).caretPosition =
//            (endpointsComboBox.selectedItem as String).length

        upperPanel.add(startLocalServerCheckBox)

        val okButton: JButton

        val buttonPanel = JPanel(MigLayout()).apply {
            okButton = addNewButton("ok", onOk, "tag ok")
            addNewButton("cancel", onCancel, "tag cancel")
        }
        SelectServerDialog.rootPane.defaultButton = okButton

        lowerPanel.add(buttonPanel, "center")

        testButton.addActionListener { onTest.publish(testButton) }
        startLocalServerCheckBox.addActionListener { onStartLocalServerCheckBoxChanged.publish(startLocalServerCheckBox) }

        NewEndpointDialog.onOk.subscribe {
            endpointsComboBox.addItem(NewEndpointDialog.newEndpoint)
        }

        onAdd.subscribe {
            NewEndpointDialog.isVisible = true
        }

        onRemove.subscribe {
            endpointsComboBox.removeItem(endpointsComboBox.selectedItem)
            if (endpointsComboBox.itemCount == 0) {
                removeButton.isEnabled = false
                okButton.isEnabled = false
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
        val disposables = ArrayList<Disposable>()

        disposables += Client.onConnected.subscribe {
            JOptionPane.showMessageDialog(
                this,
                MESSAGES.get("connected_successfully_to_server")
            )

            Client.close()

            disposables.forEach { it.dispose() }
            disposables.clear()
        }

        disposables += Client.onDisconnected.subscribe {
            cursor = Cursor.getDefaultCursor()
        }

        disposables += Client.onError.subscribe {
            JOptionPane.showMessageDialog(
                this,
                MESSAGES.get("could_not_connect_to_server"),
                MESSAGES.get("title_warning"),
                JOptionPane.WARNING_MESSAGE
            )

            cursor = Cursor.getDefaultCursor()

            disposables.forEach { it.dispose() }
            disposables.clear()
        }

        var endpoint = endpointsComboBox.selectedItem as String
        if (!endpoint.contains("//:")) {
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

        startLocalServerCheckBox.isSelected = ServerSettings.startLocalServer
    }

    private fun saveServerConfig() {
        ServerSettings.endpoint = endpointsComboBox.selectedItem as String

        val userEndpoints = ArrayList<String>()
        val size = endpointsComboBox.itemCount
        for (i in 0 until size) {
            userEndpoints.add(endpointsComboBox.getItemAt(i))
        }
        ServerSettings.userEndpoints = userEndpoints

        ServerSettings.startLocalServer = startLocalServerCheckBox.isSelected

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