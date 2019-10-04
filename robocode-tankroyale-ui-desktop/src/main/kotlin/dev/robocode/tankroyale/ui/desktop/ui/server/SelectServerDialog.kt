package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewButton
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewLabel
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
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

    private val urlComboBox = JComboBox(arrayOf("localhost:55000"))
    private val addButton = addNewButton("add", onAdd)
    private val removeButton = addNewButton("remove", onRemove)
    private val testButton = addNewButton("server_test", onTest)

    init {
        val upperPanel = JPanel(MigLayout("", "[][grow][][][]")).apply {
            addNewLabel("url")
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
            okButton = addNewButton("ok", onOk, "tag ok")
            addNewButton("cancel", onCancel, "tag cancel")
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

        var disposable: Closeable? = null
        disposable = testServerCommand.onCompleted.subscribe {
            testConnectionRunning = false
            disposable?.close()
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