package dev.robocode.tankroyale.ui.desktop.ui.components

import java.awt.event.MouseEvent
import javax.swing.JTable
import javax.swing.table.AbstractTableModel

class JTooltipTable(model: AbstractTableModel) : JTable(model) {

    override fun getToolTipText(e: MouseEvent): String? {
        var tip: String? = null
        val p = e.point
        val rowIndex = rowAtPoint(p)
        val colIndex = columnAtPoint(p)
        try {
            // Wrap value into <html> tag and replace newlines with <br> tags to get a multi-line tip
            val value = getValueAt(rowIndex, colIndex) ?: return null
            val text = "" + value
            tip = "<html>$text</html>"
        } catch (ex: RuntimeException) {
            // catch exception if mouse is over an empty line
        }
        return tip
    }
}