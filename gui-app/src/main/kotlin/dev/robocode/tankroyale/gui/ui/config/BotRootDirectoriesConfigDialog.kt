package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.ui.MainWindow
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.EventQueue
import javax.swing.*

object BotRootDirectoriesConfigDialog : RcDialog(MainWindow, "bot_root_directories_config_dialog") {

    init {
        contentPane.add(BotDirectoryConfigPanel)
        pack()
        setLocationRelativeTo(MainWindow) // center on main window

        onClosing {
            ConfigSettings.botDirectories = BotDirectoryConfigPanel.listModel.elements().toList()
        }
    }
}

private object BotDirectoryConfigPanel : JPanel(MigLayout("fill")) {

    private val onAdd = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onDismiss = Event<JButton>()

    val listModel = DefaultListModel<String>()
    val list = JList(listModel)
    val scrollPane = JScrollPane(list)

    init {
        addLabel("bot_root_dirs", "wrap")
        add(scrollPane, "span 2, grow, wrap")

        val buttonPanel = JPanel(MigLayout("", "[][][5][]"))
        buttonPanel.addButton("add", onAdd)
        buttonPanel.addButton("remove", onRemove)
        val dismissButton = buttonPanel.addButton("dismiss", onDismiss, "skip").apply {
            setDefaultButton(this)
        }
        add(buttonPanel)

        ConfigSettings.botDirectories.forEach { listModel.addElement(it) }

        onAdd.subscribe(this) {
            val chooser = JFileChooser()
            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            val returnVal = chooser.showOpenDialog(this)
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                val path = chooser.selectedFile.toPath()
                listModel.addElement(path.toString())
                updateSettings()
            }
        }

        onRemove.subscribe(this) {
            list.selectedValuesList.forEach { listModel.removeElement(it) }
            updateSettings()
        }

        onDismiss.subscribe(this) {
            BotRootDirectoriesConfigDialog.dispose()
        }

        BotRootDirectoriesConfigDialog.onActivated {
            dismissButton.requestFocus()
        }
    }

    fun updateSettings() {
        ConfigSettings.botDirectories = listModel.elements().toList()
    }
}

private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        BotRootDirectoriesConfigDialog.isVisible = true
    }
}
