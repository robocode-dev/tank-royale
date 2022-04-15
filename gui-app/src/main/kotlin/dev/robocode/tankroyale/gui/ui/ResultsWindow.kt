package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.model.BotResults
import dev.robocode.tankroyale.gui.ui.components.RcFrame
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

class ResultsWindow(results: List<BotResults>) : RcFrame(getWindowTitle()) {

    init {
        val table = JTable(getData(results), getColumns())
        val tableSize = Dimension(800, table.model.rowCount * table.rowHeight)

        table.apply {
            preferredScrollableViewportSize = tableSize
            preferredSize = tableSize
            minimumSize = tableSize
        }

        val tableHeader = table.tableHeader
        val headerFontMetrics = tableHeader.getFontMetrics(tableHeader.font)

        val columnModel = tableHeader.columnModel

        for (columnIndex in 0 until columnModel.columnCount) {
            val column = columnModel.getColumn(columnIndex)
            val title = "" + column.headerValue
            column.minWidth = 10 + headerFontMetrics.stringWidth(title)

            val cellRenderer = DefaultTableCellRenderer()
            cellRenderer.horizontalAlignment = JLabel.CENTER
            column.cellRenderer = cellRenderer
        }

        val scrollPane = JScrollPane(table)

        contentPane.add(scrollPane)
        pack()
        setLocationRelativeTo(MainWindow) // center on main window
    }

    private fun getData(results: List<BotResults>): Array<Array<String>> {
        val list = ArrayList<Array<String>>()
        results.forEach {
            val name = "${it.name} ${it.version}"
            list.add(
                arrayOf(
                    "" + it.rank,
                    "" + name,
                    "" + it.totalScore,
                    "" + it.survival,
                    "" + it.lastSurvivorBonus,
                    "" + it.bulletDamage,
                    "" + it.bulletKillBonus,
                    "" + it.ramDamage,
                    "" + it.ramKillBonus,
                    "" + it.firstPlaces,
                    "" + it.secondPlaces,
                    "" + it.thirdPlaces
                )
            )
        }
        return list.toArray(arrayOfNulls<Array<String>>(list.size))
    }

    private fun getColumns(): Array<String> {
        Strings.apply {
            return arrayOf(
                get("results.rank"),
                get("results.bot_name"),
                get("results.total_score"),
                get("results.survival_score"),
                get("results.last_survivor_bonus"),
                get("results.bullet_damage_score"),
                get("results.bullet_kill_bonus"),
                get("results.ram_damage"),
                get("results.ram_kill_bonus"),
                get("results.firsts"),
                get("results.seconds"),
                get("results.thirds")
            )
        }
    }
}

private fun getWindowTitle(): String {
    val numberOfRounds: Int = Client.currentGameSetup?.numberOfRounds ?: 0
    return UiTitles.get("results_window").replace("$1", "$numberOfRounds")
}