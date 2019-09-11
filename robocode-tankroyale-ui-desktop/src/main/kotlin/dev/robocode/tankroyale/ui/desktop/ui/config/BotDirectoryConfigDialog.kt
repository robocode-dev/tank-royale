package dev.robocode.tankroyale.ui.desktop.ui.config

import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewButton
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewLabel
import dev.robocode.tankroyale.ui.desktop.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.ui.desktop.settings.MiscSettings
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.utils.Event
import kotlinx.serialization.ImplicitReflectionSerializer
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.*

@ImplicitReflectionSerializer
object BotDirectoryConfigDialog : JDialog(MainWindow, getWindowTitle()) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(500, 200)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(BotDirectoryConfigPanel)

        onClosing() {
            MiscSettings.botsDirectories = BotDirectoryConfigPanel.listModel.elements().toList()
            MiscSettings.save()
        }
    }
}

private fun getWindowTitle(): String {
    return ResourceBundles.UI_TITLES.get("bot_directory_config_dialog")
}

private object BotDirectoryConfigPanel : JPanel(MigLayout("fill")) {

    // Private events
    private val onAdd = Event<JButton>()
    private val onRemove = Event<JButton>()

    val listModel = DefaultListModel<String>()
    val list = JList<String>(listModel)
    val scrollPane = JScrollPane(list)

    init {

        addNewLabel("bot_directories", "wrap")
        add(scrollPane, "span 2, grow, wrap")

        val buttonPanel = JPanel()
        buttonPanel.addNewButton("add", onAdd)
        buttonPanel.addNewButton("remove", onRemove)
        add(buttonPanel)

        MiscSettings.load()
        MiscSettings.botsDirectories.forEach { listModel.addElement(it) }

        onAdd.subscribe {
            val chooser = JFileChooser()
            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            val returnVal = chooser.showOpenDialog(this)
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                val path = chooser.selectedFile.toPath()
                listModel.addElement(path.toString())
            }
        }

        onRemove.subscribe {
            list.selectedValuesList.forEach { listModel.removeElement(it) }
        }
    }
}

@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        BotDirectoryConfigDialog.isVisible = true
    }
}
