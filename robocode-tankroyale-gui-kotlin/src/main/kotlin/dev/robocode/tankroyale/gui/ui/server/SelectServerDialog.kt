package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showMessage
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.MainWindow
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.ui.new_server.CheckWebSocketConnection
import dev.robocode.tankroyale.gui.ui.new_server.WsUrl
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.*

object SelectServerDialog : JDialog(MainWindow, ResourceBundles.UI_TITLES.get("select_server_dialog")) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(500, 150)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(SelectServerPanel)
    }
}

private object SelectServerPanel : JPanel(MigLayout("fill")) {

    // Private events
    private val onAdd = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onTest = Event<JButton>()

    private val onOk = Event<JButton>()
    private val onCancel = Event<JButton>()

    private val urlComboBox = JComboBox(arrayOf(ServerSettings.DEFAULT_URL))
    private val addButton = addButton("add", onAdd)
    private val removeButton = addButton("remove", onRemove)
    private val testButton = addButton("server_test", onTest)

    init {
        val upperPanel = JPanel(MigLayout("", "[][grow][][][]")).apply {
            addLabel("url")
            add(urlComboBox, "span 2, grow")
            add(addButton)
            add(removeButton)
            add(testButton, "wrap")
        }
        val lowerPanel = JPanel(MigLayout("", "[grow]"))

        add(upperPanel, "north")
        add(lowerPanel, "south")

        val okButton: JButton

        val buttonPanel = JPanel(MigLayout()).apply {
            okButton = addButton("ok", onOk, "tag ok")
            addButton("cancel", onCancel, "tag cancel")
        }
        SelectServerDialog.rootPane.defaultButton = okButton

        lowerPanel.add(buttonPanel, "center")

        AddNewUrlDialog.onComplete.subscribe {
            urlComboBox.addItem(AddNewUrlDialog.newUrl)
            selectedUri = AddNewUrlDialog.newUrl

            removeButton.isEnabled = true
            okButton.isEnabled = true
            testButton.isEnabled = true
        }

        onAdd.subscribe {
            AddNewUrlDialog.isVisible = true
        }

        onRemove.subscribe {
            urlComboBox.removeItem(selectedUri)
            if (urlComboBox.itemCount == 0) {
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

    private var selectedUri
        get() = urlComboBox.selectedItem as String
        set(value) { setSelectedItem(value) }

    private fun testServerConnection() {
        if (CheckWebSocketConnection.isRunning(selectedUri)) {
            showMessage(ResourceBundles.STRINGS.get("server_is_running"))
        } else {
            showMessage(ResourceBundles.STRINGS.get("server_not_found"))
        }
    }

    private fun setFieldsToServerConfig() {
        urlComboBox.removeAllItems()

        if (ServerSettings.userUrls.isNotEmpty()) {
            ServerSettings.userUrls.forEach { urlComboBox.addItem(it) }
        } else {
            urlComboBox.addItem(ServerSettings.serverUrl)
        }
        selectedUri = ServerSettings.serverUrl
    }

    // This method is required as setSelectedItem() does not work as the url can be partial
    private fun setSelectedItem(selectedItem: String) {
        for (i in 0 until urlComboBox.itemCount) {
            val item = urlComboBox.getItemAt(i)
            if (WsUrl(item) == WsUrl(selectedItem)) {
                urlComboBox.selectedIndex = i
                return
            }
        }
    }

    private fun saveServerConfig() {
        ServerSettings.serverUrl = selectedUri

        val userUrls = ArrayList<String>()
        val size = urlComboBox.itemCount
        for (i in 0 until size) {
            userUrls.add(urlComboBox.getItemAt(i))
        }
        ServerSettings.userUrls = userUrls

        ServerSettings.save()
    }
}

private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        SelectServerDialog.isVisible = true
    }
}