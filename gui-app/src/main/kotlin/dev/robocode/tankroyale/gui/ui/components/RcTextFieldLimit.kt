package dev.robocode.tankroyale.gui.ui.components

import javax.swing.text.AttributeSet
import javax.swing.text.PlainDocument

class RcTextFieldLimit(private var limit: Int) : PlainDocument() {

    override fun insertString(offset: Int, str: String?, attr: AttributeSet?) {
        if (str != null && length + str.length <= limit) {
            super.insertString(offset, str, attr)
        }
    }
}