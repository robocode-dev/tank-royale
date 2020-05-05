package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel


class BotSelectionTable : JTable(BotSelectionTableModel()) {
    init {
        setDefaultRenderer(BotInfo::class.java, BotInfoTableCellRenderer())
        setDefaultRenderer(BotAvailability::class.java, BotAvailabilityTableCellRenderer())

        tableHeader = null // disable table header
        setShowGrid(false) // disable grid
        intercellSpacing = Dimension(0, 0) // disable inter spacing

        // Set the last column to a fixed size
        val lastCol = columnModel.getColumn(1)
        lastCol.preferredWidth = 50
        lastCol.maxWidth = 50
    }

    fun clear() {
        (model as DefaultTableModel).rowCount = 0
    }

    fun add(botInfo: BotInfo, availability: BotAvailability) {
        (model as DefaultTableModel).addRow(arrayOf(botInfo, availability));
    }

    fun add(row: BotSelectionTableRow) {
        add(row.botInfo, row.availability)
    }

    fun removeAt(row: Int) {
        (model as DefaultTableModel).removeRow(row)
    }

    fun contains(botInfo: BotInfo): Boolean {
        return rows().any { it.botInfo === botInfo }
    }

    fun rows(): List<BotSelectionTableRow> {
        val list = ArrayList<BotSelectionTableRow>()
        for (i in 0 until rowCount) {
            list.add(get(i))
        }
        return list;
    }

    fun selected(): List<BotSelectionTableRow> {
        val selected = ArrayList<BotSelectionTableRow>()
        selectedRows.forEach { selected.add(get(it)) }
        return selected
    }

    fun selectedIndices(): List<Int> {
        return selectedRows.toList()
    }

    operator fun get(row: Int): BotSelectionTableRow {
        return BotSelectionTableRow(
            model.getValueAt(row, 0) as BotInfo,
            model.getValueAt(row, 1) as BotAvailability
        )
    }
}

enum class BotAvailability(val availability: String) {
    OFFLINE("offline"),
    READY("ready")
}

class BotSelectionTableRow(val botInfo: BotInfo, val availability: BotAvailability)

internal class BotSelectionTableModel : DefaultTableModel(0, 2) {
    override fun getColumnClass(columnIndex: Int): Class<*>? {
        return when (columnIndex) {
            0 -> BotInfo::class.java
            1 -> BotAvailability::class.java
            else -> String::class.java
        }
    }
}

internal class BotInfoTableCellRenderer : DefaultTableCellRenderer() {
    override fun getTableCellRendererComponent(
        table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, col: Int
    ): Component {
        val botInfo = table.model.getValueAt(row, col) as BotInfo
        return super.getTableCellRendererComponent(table, botInfo.displayText, isSelected, hasFocus, row, col)
    }
}

internal class BotAvailabilityTableCellRenderer : DefaultTableCellRenderer() {
    override fun getTableCellRendererComponent(
        table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, col: Int
    ): Component {
        val avail = (table.model.getValueAt(row, col) as BotAvailability)
        val component = super.getTableCellRendererComponent(table, avail.availability, isSelected, hasFocus, row, col)

        if (!isSelected) {
            component.background = when (avail) {
                BotAvailability.READY -> Color(0xAAFFAA)
                BotAvailability.OFFLINE -> Color(0xFFAAAA)
            }
        }
        return component;
    }
}