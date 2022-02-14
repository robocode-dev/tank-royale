package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.MainWindow
import dev.robocode.tankroyale.gui.settings.MiscSettings
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.EventQueue
import javax.swing.*

object BotDirectoryConfigDialog :
    JDialog(MainWindow, ResourceBundles.UI_TITLES.get("bot_root_directories_config_dialog")) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        contentPane.add(BotDirectoryConfigPanel)
        pack()
        setLocationRelativeTo(MainWindow) // center on main window

        onClosing {
            MiscSettings.setBotDirectories(BotDirectoryConfigPanel.listModel.elements().toList())
        }
    }
}

private object BotDirectoryConfigPanel : JPanel(MigLayout("fill")) {

    // Private events
    private val onAdd = Event<JButton>()
    private val onRemove = Event<JButton>()

    val listModel = DefaultListModel<String>()
    val list = JList(listModel)
    val scrollPane = JScrollPane(list)

    init {
        addLabel("bot_root_dirs", "wrap")
        add(scrollPane, "span 2, grow, wrap")

        val buttonPanel = JPanel()
        buttonPanel.addButton("add", onAdd)
        buttonPanel.addButton("remove", onRemove)
        add(buttonPanel)

        MiscSettings.getBotDirectories().forEach { listModel.addElement(it) }

        onAdd.subscribe(BotDirectoryConfigDialog) {
            val chooser = JFileChooser()
            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            val returnVal = chooser.showOpenDialog(this)
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                val path = chooser.selectedFile.toPath()
                listModel.addElement(path.toString())
                updateSettings()
            }
        }

        onRemove.subscribe(BotDirectoryConfigDialog) {
            list.selectedValuesList.forEach { listModel.removeElement(it) }
            updateSettings()
        }
    }

    fun updateSettings() {
        MiscSettings.setBotDirectories(listModel.elements().toList())
    }
}

private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        BotDirectoryConfigDialog.isVisible = true
    }
}
