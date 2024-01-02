package dev.robocode.tankroyale.gui.ui.components

import javax.swing.JTextField

open class RcLimitedTextField(columns: Int, text: String? = null) : JTextField(RcTextFieldLimit(columns), text, columns) {
    override fun createToolTip() = RcToolTip()
}