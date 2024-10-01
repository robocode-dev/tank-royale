package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.settings.BotDirectoryConfig
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.config.BotDirectoryConfigPanel.listModel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.gui.util.EDT
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.event.*
import javax.swing.*

object BotRootDirectoriesConfigDialog : RcDialog(MainFrame, "bot_root_directories_config_dialog") {

    init {
        preferredSize = Dimension(400, 300)

        contentPane.add(BotDirectoryConfigPanel)
        pack()
        setLocationRelativeTo(owner) // center on owner window

        onClosing {
            ConfigSettings.botDirectories =
                listModel.elements().toList().map { BotDirectoryConfig(it.label, it.isActive) }
        }
    }
}

private object BotDirectoryConfigPanel : JPanel(MigLayout("fill")) {

    private val onAdd = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onOk = Event<JButton>()

    val listModel = DefaultListModel<CheckListEntity>()
    val list = JList(listModel)
    val scrollPane = JScrollPane(list)

    init {
        list.cellRenderer = CheckListRenderer()
        list.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION

        add(scrollPane, "span, grow, wrap")

        scrollPane.border = BorderFactory.createTitledBorder(Strings.get("bot_root_dirs"))

        val buttonPanel = JPanel(MigLayout("center, insets 0")).apply {
            addButton("add", onAdd)
            addButton("remove", onRemove)
        }
        add(buttonPanel, "wrap")

        val okButton = addOkButton(onOk, "center").apply {
            setDefaultButton(this)
        }

        ConfigSettings.botDirectories.forEach { listModel.addElement(CheckListEntity(it.path, it.enabled)) }

        onAdd.subscribe(BotRootDirectoriesConfigDialog) { addDirectory() }
        onRemove.subscribe(BotRootDirectoriesConfigDialog) { removeDirectory() }
        onOk.subscribe(BotRootDirectoriesConfigDialog) { BotRootDirectoriesConfigDialog.dispose() }

        BotRootDirectoriesConfigDialog.onActivated {
            okButton.requestFocus()
        }

        list.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                if (MouseEvent.BUTTON1 == event.button) {
                    toggle(list.locationToIndex(event.getPoint()))
                }
            }
        })

        list.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(event: KeyEvent) {
                if (event.keyCode == KeyEvent.VK_SPACE) {
                    toggle(list.leadSelectionIndex)
                }
            }
        })
    }

    private fun addDirectory() {
        val directoryChooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            isMultiSelectionEnabled = true
        }
        val result = directoryChooser.showOpenDialog(this)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFiles = directoryChooser.selectedFiles
            if (selectedFiles.isEmpty()) {
                return
            }
            selectedFiles.forEach { file ->
                val selectedPath = file.toPath()

                // If the directory has already been added, make sure to enable it
                var found = false
                listModel.elements().iterator().forEach {
                    if (it.label == selectedPath.toString()) {
                        it.isActive = true
                        found = true
                    }
                }
                // Else, we just add the directory
                if (!found) {
                    val existingEntries = listModel.elements().toList().map { it.label }
                    if (!existingEntries.contains(selectedPath.toString())) {
                        listModel.addElement(CheckListEntity(selectedPath.toString(), true))
                    }
                }
            }
            EDT.enqueue { list.repaint() }
            updateSettings()
        }
    }

    private fun removeDirectory() {
        list.selectedValuesList.forEach { listModel.removeElement(it) }
        updateSettings()
    }

    private fun toggle(index: Int) {
        if (index >= 0) {
            val entity = list.model.getElementAt(index)
            entity.isActive = !entity.isActive
            updateSettings()

            EDT.enqueue { list.repaint(list.getCellBounds(index, index)) }
        }
    }

    fun updateSettings() {
        ConfigSettings.botDirectories = listModel.elements().toList().map { BotDirectoryConfig(it.label, it.isActive) }
    }
}