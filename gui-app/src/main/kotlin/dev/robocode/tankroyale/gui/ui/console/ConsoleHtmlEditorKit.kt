package dev.robocode.tankroyale.gui.ui.console

import javax.swing.text.html.HTMLEditorKit

open class ConsoleHtmlEditorKit : HTMLEditorKit() {
    init {
        styleSheet = ConsoleStyleSheet()
    }
}