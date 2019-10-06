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
            var value = getValueAt(rowIndex, colIndex) as String
            value = value.replace("\n", "<br>")
            tip = "<html>$value</html>"
        } catch (ex: RuntimeException) {
            //catch null pointer exception if mouse is over an empty line
        }
        return tip
    }
}