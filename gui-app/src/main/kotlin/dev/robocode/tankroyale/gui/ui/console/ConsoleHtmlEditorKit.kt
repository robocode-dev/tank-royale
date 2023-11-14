package dev.robocode.tankroyale.gui.ui.console

import javax.swing.text.html.HTMLEditorKit

open class ConsoleHtmlEditorKit : HTMLEditorKit() {

    init {
        styleSheet.apply {
            addRule("body { background-color: #282828; padding: 4px; }")
            addRule("span { color: white; font-family: monospace; font-size: 12;}")

            // Bot console
            addRule(".${CssClass.INFO.className} { color: \"#377B37\"; }") // olive green
            addRule(".${CssClass.ERROR.className} { color: \"#FF5733\"; }") // dark pink
            addRule(".${CssClass.LINE_NUMBER.className} { color: gray; }")

            // ANSI colors
            addRule(".esc.black { color: Black }")
            addRule(".esc.red { color: Red }")
            addRule(".esc.green { color: Green }")
            addRule(".esc.yellow { color: Yellow }")
            addRule(".esc.blue { color: Blue }")
            addRule(".esc.magenta { color: Magenta }")
            addRule(".esc.cyan { color: Cyan }")
            addRule(".esc.white { color: LightGray }")

            addRule(".esc.bright.black { color: DarkGray }")
            addRule(".esc.bright.red { color: LightRed }")
            addRule(".esc.bright.green { color: LightGreen }")
            addRule(".esc.bright.yellow { color: LightYellow }")
            addRule(".esc.bright.blue { color: LightBlue }")
            addRule(".esc.bright.magenta { color: LightMagenta }")
            addRule(".esc.bright.cyan { color: LightCyan }")
            addRule(".esc.bright.white { color: White }")
        }
    }
}