package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.ui.components.ConsoleHtmlEditorKit

class BotConsoleHtmlEditorKit : ConsoleHtmlEditorKit() {

    init {
        styleSheet.apply {
            addRule("body { color: white; font-family: monospace; }")
            addRule(".info { color: \"#377B37\"; }") // olive green
            addRule(".error { color: \"#FF5733\"; }") // dark pink
            addRule(".linenumber { color: gray; }")
        }
    }
}