package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.ui.desktop.ui.extensions.JComponentExt.showMessage
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.util.Event
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.io.Closeable
import javax.swing.*

@UnstableDefault
@ImplicitReflectionSerializer
object SelectServerDialog : JDialog(MainWindow, ResourceBundles.UI_TITLES.get("select_server_dialog")) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(500, 150)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(SelectServerPanel)
    }
}

@ImplicitReflectionSerializer
private object SelectServerPanel : JPanel(MigLayout("fill")) {

    // Private events
    private val onAdd = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onTest = Event<JButton>()

    private val onOk = Event<JButton>()
    private val onCancel = Event<JButton>()

    private val urlComboBox = JComboBox(arrayOf(ServerSettings.DEFAULT_LOCALHOST_URL))
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
            urlComboBox.selectedItem = AddNewUrlDialog.newUrl

            removeButton.isEnabled = true
            okButton.isEnabled = true
            testButton.isEnabled = true
        }

        onAdd.subscribe {
            AddNewUrlDialog.isVisible = true
        }

        onRemove.subscribe {
            urlComboBox.removeItem(urlComboBox.selectedItem)
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

    private var testConnectionRunning = false

    private fun testServerConnection() {
        if (testConnectionRunning)
            return

        val testServerCommand = TestServerConnectionCommand(urlComboBox.selectedItem as String)

        var foundDisposable: Closeable? = null
        foundDisposable = testServerCommand.onFound.subscribe {
            showMessage(ResourceBundles.STRINGS.get("server_is_running"))

            testConnectionRunning = false
            foundDisposable?.close()
        }

        var notFoundDisposable: Closeable? = null
        notFoundDisposable = testServerCommand.onNotFound.subscribe {
            showMessage(ResourceBundles.STRINGS.get("server_not_found"))

            testConnectionRunning = false
            notFoundDisposable?.close()
        }

        testServerCommand.execute()
        testConnectionRunning = true
    }

    private fun setFieldsToServerConfig() {
        urlComboBox.removeAllItems()

        if (ServerSettings.userUrls.isNotEmpty()) {
            ServerSettings.userUrls.forEach { urlComboBox.addItem(it) }
        } else {
            urlComboBox.addItem(ServerSettings.defaultUrl)
        }
        urlComboBox.selectedItem = ServerSettings.defaultUrl
    }

    private fun saveServerConfig() {
        ServerSettings.defaultUrl = urlComboBox.selectedItem as String

        val userUrls = ArrayList<String>()
        val size = urlComboBox.itemCount
        for (i in 0 until size) {
            userUrls.add(urlComboBox.getItemAt(i))
        }
        ServerSettings.userUrls = userUrls

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