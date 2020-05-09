package dev.robocode.tankroyale.ui.desktop.extensions

import javax.swing.JTable

object JTableExt {
    fun JTable.onChanged(handler: () -> Unit) {
        model.addTableModelListener {
            handler.invoke()
        }
    }

    fun JTable.onSelection(handler: (Int) -> Unit) {
        selectionModel.addListSelectionListener {
            val row = it.firstIndex
            if (row >= 0 && row < model.rowCount) {
                handler.invoke(row)
            }
        }
    }
}