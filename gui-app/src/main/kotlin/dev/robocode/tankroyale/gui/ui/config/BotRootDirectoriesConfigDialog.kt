package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import javax.swing.*

object BotRootDirectoriesConfigDialog : RcDialog(MainFrame, "bot_root_directories_config_dialog") {

    init {
        preferredSize = Dimension(400, 300)

        contentPane.add(BotDirectoryConfigPanel)
        pack()
        setLocationRelativeTo(MainFrame) // center on main window

        onClosing {
            ConfigSettings.botDirectories = BotDirectoryConfigPanel.listModel.elements().toList()
        }
    }
}

private object BotDirectoryConfigPanel : JPanel(MigLayout("fill")) {

    private val onAdd = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onOk = Event<JButton>()

    val listModel = DefaultListModel<String>()
    val list = JList(listModel)
    val scrollPane = JScrollPane(list)

    init {
        addLabel("bot_root_dirs", "wrap")
        add(scrollPane, "span, grow, wrap")

        val buttonPanel = JPanel(MigLayout("center, insets 0")).apply {
            addButton("add", onAdd)
            addButton("remove", onRemove)
        }
        add(buttonPanel, "wrap")

        val okButton = addButton("ok", onOk, "center").apply {
            setDefaultButton(this)
        }

        ConfigSettings.botDirectories.forEach { listModel.addElement(it) }

        onAdd.subscribe(BotRootDirectoriesConfigDialog) {
            val chooser = JFileChooser()
            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            val returnVal = chooser.showOpenDialog(this)
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                val path = chooser.selectedFile.toPath()
                listModel.addElement(path.toString())
                updateSettings()
            }
        }

        onRemove.subscribe(BotRootDirectoriesConfigDialog) {
            list.selectedValuesList.forEach { listModel.removeElement(it) }
            updateSettings()
        }

        onOk.subscribe(BotRootDirectoriesConfigDialog) {
            BotRootDirectoriesConfigDialog.dispose()
        }

        BotRootDirectoriesConfigDialog.onActivated {
            okButton.requestFocus()
        }
    }

    fun updateSettings() {
        ConfigSettings.botDirectories = listModel.elements().toList()
    }
}