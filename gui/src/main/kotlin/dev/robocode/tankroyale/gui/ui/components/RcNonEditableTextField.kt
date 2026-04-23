package dev.robocode.tankroyale.gui.ui.components

import javax.swing.JTextField

open class RcNonEditableTextField : JTextField() {
    init {
        isEditable = false
    }
}
