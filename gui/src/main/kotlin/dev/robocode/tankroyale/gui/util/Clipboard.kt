package dev.robocode.tankroyale.gui.util

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object Clipboard {

    fun set(text: String?) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    }
}