package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.model.Results
import dev.robocode.tankroyale.gui.ui.components.RcFrame
import dev.robocode.tankroyale.gui.ui.components.RcToolTip
import java.awt.Component
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

class ResultsFrame(results: List<Results>) : RcFrame(getWindowTitle(), isTitlePropertyName = false) {

    init {
        val table = object : JTable(getData(results), getColumns()) {
            override fun createToolTip() = RcToolTip()
        }
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
            column.minWidth = headerFontMetrics.stringWidth(title)
            column.cellRenderer = CellRendererWithToolTip

            if (columnIndex == 1) {
                column.minWidth = 120 // Name needs more width
            }
        }

        val scrollPane = JScrollPane(table)

        contentPane.add(scrollPane)
        pack()
        setLocationRelativeTo(owner) // center on owner window
    }

    private fun getData(results: List<Results>): Array<Array<String>> {
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
                get("results.name"),
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

internal object CellRendererWithToolTip : DefaultTableCellRenderer() {
    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column).apply {
        horizontalAlignment = JLabel.CENTER
        toolTipText = value?.toString()
    }
}
