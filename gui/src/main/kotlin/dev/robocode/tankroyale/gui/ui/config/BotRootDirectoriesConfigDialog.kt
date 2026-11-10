package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.rumble.BotFolderSetting
import dev.robocode.tankroyale.rumble.DownloadFolders
import dev.robocode.tankroyale.common.event.Event
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
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

object BotRootDirectoriesConfigDialog : RcDialog(MainFrame, "bot_root_directories_config_dialog") {

    init {
        preferredSize = Dimension(400, 300)

        contentPane.add(BotDirectoryConfigPanel)
        pack()
        setLocationRelativeTo(owner) // center on owner window

        onClosing {
            BotDirectoryConfigPanel.updateSettings()
        }
    }

    override fun setVisible(visible: Boolean) {
        if (visible && !isVisible) BotDirectoryConfigPanel.reload()
        super.setVisible(visible)
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
        // A labelled folder says what it holds, beside its path.
        // The labels are read when the list is read, not for every row painted.
        list.cellRenderer = CheckListRenderer { path ->
            if (DownloadFolders.isLabelled(path, labels)) " " + String.format(Strings.get("bot_root_dirs.labelled"), Strings.get("rumble_downloads_label")) else ""
        }
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

        // The dialog saves each change at once, so it holds nothing unsaved. When the settings are saved elsewhere while
        // it is open, for example when a download adds its folder, it reads them again, so it never writes back an old list.
        ConfigSettings.onSaved.on(this) {
            if (!saving && BotRootDirectoriesConfigDialog.isVisible) reload()
        }

        onAdd.on(BotRootDirectoriesConfigDialog) { addDirectory() }
        onRemove.on(BotRootDirectoriesConfigDialog) { removeDirectory() }
        onOk.on(BotRootDirectoriesConfigDialog) { BotRootDirectoriesConfigDialog.dispose() }

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

                // If the directory has already been added, however its path is written, make sure to enable it
                val key = DownloadFolders.key(selectedPath.toString())
                val existing = listModel.elements().toList().filter { DownloadFolders.key(it.label) == key }
                existing.forEach { it.isActive = true }
                // Else, we just add the directory
                if (existing.isEmpty()) {
                    listModel.addElement(CheckListEntity(selectedPath.toString(), true))
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
        val folders = listModel.elements().toList().map { BotFolderSetting(it.label, it.isActive) }
        // A removed folder leaves no label behind; both are saved in one write.
        labels = DownloadFolders.keepLabelsOf(folders, ConfigSettings.botDirectoryLabels)
        saving = true
        try {
            ConfigSettings.saveBotFolders(folders, labels)
        } finally {
            saving = false
        }
    }

    /** True while this dialog saves, so its own save does not make it read the list again. */
    private var saving = false

    /** The labels of the bot directories as last read or saved. */
    private var labels: Map<String, String> = ConfigSettings.botDirectoryLabels

    /**
     * Reads the bot directories again, because they may have changed while the dialog was closed, for example when a
     * download added its folder. Without it, closing the dialog would write back an old list.
     */
    fun reload() {
        labels = ConfigSettings.botDirectoryLabels
        listModel.clear()
        ConfigSettings.botDirectories.forEach { listModel.addElement(CheckListEntity(it.path, it.enabled)) }
    }
}
