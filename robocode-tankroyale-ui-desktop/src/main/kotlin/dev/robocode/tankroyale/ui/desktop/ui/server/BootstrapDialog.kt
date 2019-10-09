package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.bootstrap.BootstrapProcess
import dev.robocode.tankroyale.ui.desktop.bootstrap.BotEntry
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles.UI_TITLES
import dev.robocode.tankroyale.ui.desktop.ui.battle.SelectBotsDialog
import dev.robocode.tankroyale.ui.desktop.ui.server.BootstrapDialog.onCancel
import dev.robocode.tankroyale.ui.desktop.ui.server.BootstrapDialog.onOk
import dev.robocode.tankroyale.ui.desktop.ui.components.JTooltipTable
import dev.robocode.tankroyale.ui.desktop.util.Event
import javafx.scene.input.MouseButton
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableModel
import kotlin.collections.ArrayList


@UnstableDefault
@ImplicitReflectionSerializer
object BootstrapDialog : JDialog(MainWindow, UI_TITLES.get("bootstrap_dialog")) {

    val onOk = Event<JButton>()
    val onCancel = Event<JButton>()

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(700, 250)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(BootstrapDialogPanel)
    }
}

@UnstableDefault
@ImplicitReflectionSerializer
private object BootstrapDialogPanel : JPanel(MigLayout("fill")) {

    @UnstableDefault
    private val botEntries: List<BotEntry> = BootstrapProcess.list()

    private val table: JTable

    private val selectedBotFiles: List<String>
        get() {
            val files = ArrayList<String>()
            table.selectedRows.forEach { row ->
                val count = ("" + table.getValueAt(row, 0)).toInt()
                repeat(count) {
                    files += botEntries[row].filename
                }
            }
            return Collections.unmodifiableList(files)
        }


    init {
        val upperPanel = JPanel(MigLayout("fill", "[fill]"))
        val lowerPanel = JPanel(MigLayout("center", ""))

        add(upperPanel, "north")
        add(lowerPanel, "south")

        upperPanel.addLabel("select_bots_to_boot", "wrap")

        val tableModel = BDTableModel()
        table = JTooltipTable(tableModel)


        val scrollPane = JScrollPane(table)
        upperPanel.add(scrollPane)

        val okButton = lowerPanel.addButton("ok", onOk, "tag ok")
        lowerPanel.addButton("cancel", onCancel, "tag cancel")

        tableModel.addColumn(UI_TITLES.get("count_column"))
        tableModel.addColumn(UI_TITLES.get("name_column"))
        tableModel.addColumn(UI_TITLES.get("version_column"))
        tableModel.addColumn(UI_TITLES.get("author_column"))
        tableModel.addColumn(UI_TITLES.get("country_code_column"))
        tableModel.addColumn(UI_TITLES.get("programming_language_column"))
        tableModel.addColumn(UI_TITLES.get("description_column"))

        val cols = table.columnModel

        cols.getColumn(0).preferredWidth = 30  // count
        cols.getColumn(1).preferredWidth = 100 // name
        cols.getColumn(2).preferredWidth = 30  // version
        cols.getColumn(3).preferredWidth = 100 // author
        cols.getColumn(4).preferredWidth = 30  // country code
        cols.getColumn(5).preferredWidth = 50  // programming language
        cols.getColumn(6).preferredWidth = 200 // description

        botEntries.forEach { entry ->
            run {
                val info = entry.info
                val list = listOf(
                    1,
                    info.name,
                    info.version,
                    info.author,
                    info.countryCode,
                    info.programmingLang,
                    info.description
                )
                tableModel.addRow(list.toTypedArray())
            }
        }

        okButton.isEnabled = false

        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {

                val row = table.rowAtPoint(event.point)
                val col = table.columnAtPoint(event.point)
                if (row >= 0 && col >= 0) {
                    if (col == 0) {
                        var count = ("" + table.getValueAt(row, 0)).toInt()
                        if (event.button == MouseButton.PRIMARY.ordinal) {
                            count += 1
                        } else if (event.button == MouseButton.SECONDARY.ordinal) {
                            count = 0.coerceAtLeast(count - 1)
                        }
                        table.setValueAt(count, row, 0)
                    }

                    okButton.isEnabled = table.selectedRowCount > 0
                }
            }
        })


        onOk.subscribe {
            bootstrapBots()
            BootstrapDialog.dispose()
        }
        onCancel.subscribe { BootstrapDialog.dispose() }
    }

    private fun bootstrapBots() {
        SelectBotsDialog.isVisible = true
        BootstrapProcess.run(selectedBotFiles)
    }

    class BDTableModel : DefaultTableModel() {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return (column == 0) // Only count column is editable
        }
    }
}

@UnstableDefault
@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        BootstrapDialog.isVisible = true
    }
}