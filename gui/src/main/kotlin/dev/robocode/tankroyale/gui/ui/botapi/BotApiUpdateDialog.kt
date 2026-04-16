package dev.robocode.tankroyale.gui.ui.botapi

import dev.robocode.tankroyale.common.util.Version
import dev.robocode.tankroyale.gui.botapi.BotApiLibEntry
import dev.robocode.tankroyale.gui.botapi.BotApiLibraryService
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Strings
import net.miginfocom.swing.MigLayout
import javax.swing.*
import javax.swing.table.AbstractTableModel

class BotApiUpdateDialog(private val entries: List<BotApiLibEntry>) : JDialog(MainFrame, true) {

    init {
        title = Strings.get("bot.api.update.dialog.title")
        defaultCloseOperation = DISPOSE_ON_CLOSE
        contentPane.layout = MigLayout("fill, insets 10", "[grow]", "[][grow][]")

        add(JLabel(Strings.get("bot.api.update.dialog.header")), "wrap, gapbottom 6")
        add(JScrollPane(createTable()), "grow, wrap, gapbottom 6")
        add(createButtonPanel(), "alignx right")

        pack()
        setLocationRelativeTo(owner)
        minimumSize = java.awt.Dimension(600, 260)
    }

    private fun createTable(): JTable {
        val columnNames = arrayOf(
            Strings.get("bot.api.update.column.bot_dir"),
            Strings.get("bot.api.update.column.platform"),
            Strings.get("bot.api.update.column.installed"),
            Strings.get("bot.api.update.column.new_version"),
        )
        val missingLabel = Strings.get("bot.api.update.missing")

        val model = object : AbstractTableModel() {
            override fun getRowCount() = entries.size
            override fun getColumnCount() = columnNames.size
            override fun getColumnName(col: Int) = columnNames[col]
            override fun isCellEditable(row: Int, col: Int) = false
            override fun getValueAt(row: Int, col: Int): Any {
                val e = entries[row]
                return when (col) {
                    0 -> e.botRootDir.toString()
                    1 -> e.platform.displayName
                    2 -> e.installedVersion ?: missingLabel
                    3 -> Version.version
                    else -> ""
                }
            }
        }

        return JTable(model).apply {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
            columnModel.getColumn(0).apply { minWidth = 280; preferredWidth = 280 }
            columnModel.getColumn(1).preferredWidth = 80
            columnModel.getColumn(2).preferredWidth = 100
            columnModel.getColumn(3).preferredWidth = 100
        }
    }

    private fun createButtonPanel(): JPanel {
        val panel = JPanel(MigLayout("insets 0", "[][]"))

        val updateButton = JButton(Strings.get("bot.api.update.update_all")).apply {
            addActionListener { onUpdateAll() }
        }
        val skipButton = JButton(Strings.get("bot.api.update.skip")).apply {
            addActionListener { dispose() }
        }
        val dontAskCheckBox = JCheckBox(Strings.get("bot.api.update.dont_ask")).apply {
            isSelected = !ConfigSettings.checkBotApiUpdates
            addActionListener { ConfigSettings.checkBotApiUpdates = !isSelected }
        }

        panel.add(dontAskCheckBox, "gapright push")
        panel.add(updateButton)
        panel.add(skipButton)

        rootPane.defaultButton = updateButton
        return panel
    }

    private fun onUpdateAll() {
        entries.forEach { entry ->
            try {
                BotApiLibraryService.update(entry)
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    this,
                    "${e.message}",
                    Strings.get("bot.api.update.error.title"),
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
        dispose()
    }
}
