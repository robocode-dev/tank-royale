package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.ui.MainWindow
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.config.AddNewUrlDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showMessage
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.WsUrl
import net.miginfocom.swing.MigLayout
import javax.swing.*

object SelectServerDialog : RcDialog(MainWindow, "select_server_dialog") {

    init {
        contentPane.add(SelectServerPanel)
        pack()
        setLocationRelativeTo(MainWindow) // center on main window
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
            okButton = addButton("ok", onOk, "tag ok").apply {
                setDefaultButton(this)
            }
            addButton("cancel", onCancel, "tag cancel")
        }
        SelectServerDialog.onActivated {
            okButton.requestFocus()
        }

        lowerPanel.add(buttonPanel, "center")

        AddNewUrlDialog.apply {
            onComplete.enqueue(this) {
                urlComboBox.addItem(newUrl)
                selectedUri = newUrl

                removeButton.isEnabled = true
                okButton.isEnabled = true
                testButton.isEnabled = true
            }
        }

        onAdd.subscribe(SelectServerDialog) {
            AddNewUrlDialog.isVisible = true
        }

        onRemove.subscribe(SelectServerDialog) {
            urlComboBox.removeItem(selectedItem)
            if (urlComboBox.itemCount == 0) {
                removeButton.isEnabled = false
                okButton.isEnabled = false
                testButton.isEnabled = false
            }
        }

        onTest.subscribe(SelectServerDialog) { testServerConnection() }

        onOk.subscribe(SelectServerDialog) {
            saveServerConfig()
            SelectServerDialog.dispose()
        }
        onCancel.subscribe(SelectServerDialog) {
            setFieldsToServerConfig()
            SelectServerDialog.dispose()
        }

        setFieldsToServerConfig()
    }


    private var selectedUri
        get() = WsUrl(selectedItem).origin
        set(value) {
            setSelectedItem(value)
        }

    private val selectedItem get() = urlComboBox.selectedItem as String

    private fun testServerConnection() {
        Strings.apply {
            if (RemoteServer.isRunning(selectedUri)) {
                showMessage(get("server_is_running"))
            } else {
                showMessage(get("server_not_found"))
            }
        }
    }

    private fun setFieldsToServerConfig() {
        urlComboBox.removeAllItems()

        with(ServerSettings) {
            if (userUrls.isNotEmpty()) {
                userUrls.forEach { urlComboBox.addItem(it) }
            } else {
                urlComboBox.addItem(serverUrl)
            }
            selectedUri = serverUrl
        }
    }

    // This method is required as setSelectedItem() does not work as the URL can be partial
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
        with (ServerSettings) {
            this.userUrls = userUrls
            save()
        }
    }
}