package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.model.BotResults
import dev.robocode.tankroyale.gui.ui.ResourceBundles.STRINGS
import dev.robocode.tankroyale.gui.ui.components.RcFrame
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.UIManager
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
        STRINGS.apply {
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
    return ResourceBundles.UI_TITLES.get("results_window").replace("$1", "$numberOfRounds")
}

private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val results: List<BotResults> = listOf(
        BotResults(
            name = "Rampage",
            version = "1.0",
            id = 1,
            rank = 1,
            totalScore = 2204,
            survival = 250,
            lastSurvivorBonus = 50,
            bulletDamage = 1724,
            bulletKillBonus = 180,
            ramDamage = 10,
            ramKillBonus = 20,
            firstPlaces = 5,
            secondPlaces = 6,
            thirdPlaces = 7
        ),
        BotResults(
            name = "Master",
            version = "0.0.2",
            rank = 2,
            id = 2,
            totalScore = 2108,
            survival = 245,
            lastSurvivorBonus = 40,
            bulletDamage = 797,
            bulletKillBonus = 21,
            ramDamage = 0,
            ramKillBonus = 0,
            firstPlaces = 3,
            secondPlaces = 1,
            thirdPlaces = 9
        )
    )

    EventQueue.invokeLater {
        ResultsWindow(results).isVisible = true
    }
}