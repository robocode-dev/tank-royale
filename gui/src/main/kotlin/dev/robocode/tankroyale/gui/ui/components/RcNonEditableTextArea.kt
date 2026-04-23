package dev.robocode.tankroyale.gui.ui.components

import javax.swing.JTextArea

open class RcNonEditableTextArea : JTextArea() {
    init {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
    }

    override fun updateUI() {
        super.updateUI()
        border = plainBorder()
        inactiveBg()?.let { background = it }
    }
}
