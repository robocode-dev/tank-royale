package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.client.model.Results
import dev.robocode.tankroyale.gui.client.Client
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
            val headerWidth = headerFontMetrics.stringWidth(title)
            column.minWidth = headerWidth
            column.cellRenderer = CellRendererWithToolTip

            when (columnIndex) {
                1 -> column.minWidth = 120 // Name needs more width
                2 -> {
                    column.minWidth = headerWidth + 10
                    column.maxWidth = headerWidth + 16
                }
            }
        }

        val scrollPane = JScrollPane(table)

        contentPane.add(scrollPane)
        pack()
        setLocationRelativeTo(owner) // center on owner window
    }

    private fun getData(results: List<Results>): Array<Array<String>> {
        return results.map { buildResultRow(it) }.toTypedArray()
    }

    private fun buildResultRow(result: Results): Array<String> {
        return arrayOf(
            result.rank.toString(),
            buildParticipantName(result),
            buildTeamIndicator(result),
            result.totalScore.toString(),
            result.survival.toString(),
            result.lastSurvivorBonus.toString(),
            result.bulletDamage.toString(),
            result.bulletKillBonus.toString(),
            result.ramDamage.toString(),
            result.ramKillBonus.toString(),
            result.firstPlaces.toString(),
            result.secondPlaces.toString(),
            result.thirdPlaces.toString()
        )
    }

    private fun buildParticipantName(result: Results): String {
        return "${result.name} ${result.version}".trim()
    }

    private fun buildTeamIndicator(result: Results): String {
        return if (isTeamResult(result)) "✓" else ""
    }

    private fun isTeamResult(result: Results): Boolean {
        return result.isTeam ?: (result.id > 0)
    }

    private fun getColumns(): Array<String> {
        Strings.apply {
            return arrayOf(
                get("results.rank"),
                get("results.name"),
                "\uD83D\uDC65", // `👥`
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
