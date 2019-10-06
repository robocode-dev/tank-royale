package dev.robocode.tankroyale.ui.desktop.ui.bootstrap

import dev.robocode.tankroyale.ui.desktop.bootstrap.BootstrapProcess
import dev.robocode.tankroyale.ui.desktop.bootstrap.BotEntry
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles.UI_TITLES
import dev.robocode.tankroyale.ui.desktop.ui.bootstrap.BootstrapDialog.onCancel
import dev.robocode.tankroyale.ui.desktop.ui.bootstrap.BootstrapDialog.onOk
import dev.robocode.tankroyale.ui.desktop.ui.components.JTooltipTable
import dev.robocode.tankroyale.ui.desktop.util.Event
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableModel
import kotlin.collections.ArrayList

@UnstableDefault
@ImplicitReflectionSerializer
object BootstrapDialog : JDialog(MainWindow, getWindowTitle()) {

    val onOk = Event<JButton>()
    val onCancel = Event<JButton>()

    val selectedBotFiles: List<String>
        get() {
            val files = ArrayList<String>()
            BootstrapDialogPanel.table.selectedRows.forEach { row ->
                files += BootstrapDialogPanel.botEntries[row].filename
            }
            return Collections.unmodifiableList(files)
        }

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(700, 250)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(BootstrapDialogPanel)
    }
}

private fun getWindowTitle(): String {
    return UI_TITLES.get("bootstrap_dialog")
}

@UnstableDefault
@ImplicitReflectionSerializer
private object BootstrapDialogPanel : JPanel(MigLayout("fill")) {

    @UnstableDefault
    val botEntries : List<BotEntry> = BootstrapProcess.list()

    val table: JTable

    init {
        val upperPanel = JPanel(MigLayout("fill", "[fill]"))
        val lowerPanel = JPanel(MigLayout("center", ""))

        add(upperPanel, "north")
        add(lowerPanel, "south")

        upperPanel.addLabel("select_bots_to_boot", "wrap")

        val tableModel = DefaultTableModel()
        table = JTooltipTable(tableModel)
        val scrollPane = JScrollPane(table)
        upperPanel.add(scrollPane)

        lowerPanel.addButton("ok", onOk,"tag ok")
        lowerPanel.addButton("cancel", onCancel,"tag cancel")

        tableModel.addColumn(UI_TITLES.get("name_column"))
        tableModel.addColumn(UI_TITLES.get("version_column"))
        tableModel.addColumn(UI_TITLES.get("author_column"))
        tableModel.addColumn(UI_TITLES.get("country_code_column"))
        tableModel.addColumn(UI_TITLES.get("programming_language_column"))
        tableModel.addColumn(UI_TITLES.get("description_column"))

        val cols = table.columnModel

        cols.getColumn(0).preferredWidth = 100 // name
        cols.getColumn(1).preferredWidth = 30 // version
        cols.getColumn(2).preferredWidth = 100 // author
        cols.getColumn(3).preferredWidth = 30 // country code
        cols.getColumn(4).preferredWidth = 50 // programming language
        cols.getColumn(5).preferredWidth = 200 // programming language

        botEntries.forEach { entry ->
            run {
                val info = entry.info
                val list = listOf(
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

        onOk.subscribe { BootstrapDialog.dispose() }
        onCancel.subscribe { BootstrapDialog.dispose() }
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