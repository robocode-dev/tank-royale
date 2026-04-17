package dev.robocode.tankroyale.gui.ui.botapi

import dev.robocode.tankroyale.common.util.Version
import dev.robocode.tankroyale.gui.botapi.BotApiLibEntry
import dev.robocode.tankroyale.gui.botapi.BotApiLibraryService
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Strings
import net.miginfocom.swing.MigLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.table.AbstractTableModel

class BotApiUpdateDialog(private val entries: List<BotApiLibEntry>) : JDialog(MainFrame, true) {

    private val checked = MutableList(entries.size) { true }

    init {
        title = Strings.get("bot.api.update.dialog.title")
        defaultCloseOperation = DISPOSE_ON_CLOSE
        contentPane.layout = MigLayout("fill, insets 10", "[grow]", "[][grow][]")

        add(JLabel(Strings.get("bot.api.update.dialog.header")), "wrap, gapbottom 6")
        add(JScrollPane(createTable()), "grow, wrap, gapbottom 6")
        add(createButtonPanel(), "alignx right")

        pack()
        setLocationRelativeTo(owner)
        minimumSize = java.awt.Dimension(650, 260)
    }

    private fun createTable(): JTable {
        val columnNames = arrayOf(
            "",
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
            override fun getColumnClass(col: Int) = if (col == 0) Boolean::class.javaObjectType else String::class.java
            override fun isCellEditable(row: Int, col: Int) = false
            override fun getValueAt(row: Int, col: Int): Any {
                val e = entries[row]
                return when (col) {
                    0 -> checked[row]
                    1 -> e.botRootDir.toString()
                    2 -> e.platform.displayName
                    3 -> e.installedVersion ?: missingLabel
                    4 -> Version.version
                    else -> ""
                }
            }
            override fun setValueAt(value: Any?, row: Int, col: Int) {
                if (col == 0 && value is Boolean) {
                    checked[row] = value
                    fireTableCellUpdated(row, col)
                }
            }
        }

        return JTable(model).apply {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
            columnModel.getColumn(0).apply { maxWidth = 30; preferredWidth = 30 }
            columnModel.getColumn(1).apply { minWidth = 280; preferredWidth = 280 }
            columnModel.getColumn(2).preferredWidth = 80
            columnModel.getColumn(3).preferredWidth = 100
            columnModel.getColumn(4).preferredWidth = 100

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    val row = rowAtPoint(e.point)
                    if (row >= 0) {
                        checked[row] = !checked[row]
                        model.fireTableCellUpdated(row, 0)
                    }
                }
            })
        }
    }

    private fun createButtonPanel(): JPanel {
        val panel = JPanel(MigLayout("insets 0", "[][]"))

        val fixSelectedButton = JButton(Strings.get("bot.api.update.fix_selected")).apply {
            addActionListener { onFixSelected() }
        }
        val fixAllButton = JButton(Strings.get("bot.api.update.fix_all")).apply {
            addActionListener { onFixAll() }
        }
        val skipButton = JButton(Strings.get("bot.api.update.skip")).apply {
            addActionListener { dispose() }
        }
        val dontAskCheckBox = JCheckBox(Strings.get("bot.api.update.dont_ask")).apply {
            isSelected = !ConfigSettings.checkBotApiUpdates
            addActionListener { ConfigSettings.checkBotApiUpdates = !isSelected }
        }

        panel.add(dontAskCheckBox, "gapright push")
        panel.add(fixSelectedButton)
        panel.add(fixAllButton)
        panel.add(skipButton)

        rootPane.defaultButton = fixAllButton
        return panel
    }

    private fun onFixSelected() {
        val selectedEntries = entries.filterIndexed { i, _ -> checked[i] }
        updateEntries(selectedEntries)
    }

    private fun onFixAll() {
        updateEntries(entries)
    }

    private fun updateEntries(entriesToUpdate: List<BotApiLibEntry>) {
        entriesToUpdate.forEach { entry ->
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
