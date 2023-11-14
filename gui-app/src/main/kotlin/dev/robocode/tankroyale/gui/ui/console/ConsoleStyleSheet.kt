package dev.robocode.tankroyale.gui.ui.console

import javax.swing.text.html.StyleSheet

class ConsoleStyleSheet : StyleSheet() {

    init {
        addRule("""
            body {
                background-color: #282828;
                padding: 4px;
            }
            span {
                color: white;
                font-family: monospace;
                font-size: 12;
            }

            // Bot console
            .${CssClass.INFO.className} {
                color: "#377B37"; // olive green
            }
            .${CssClass.ERROR.className} {
                color: "#FF5733"; // dark pink
            }
            .${CssClass.LINE_NUMBER.className} {
                color: gray;
            }

            // ANSI colors
            .esc.black   { color: Black }
            .esc.red     { color: Red }
            .esc.green   { color: Green }
            .esc.yellow  { color: Yellow }
            .esc.blue    { color: Blue }
            .esc.magenta { color: Magenta }
            .esc.cyan    { color: Cyan }
            .esc.white   { color: LightGray }
    
            .esc.bright.black   { color: DarkGray }
            .esc.bright.red     { color: LightRed }
            .esc.bright.green   { color: LightGreen }
            .esc.bright.yellow  { color: LightYellow }
            .esc.bright.blue    { color: LightBlue }
            .esc.bright.magenta { color: LightMagenta }
            .esc.bright.cyan    { color: LightCyan }
            .esc.bright.white   { color: White }
        """
        )
    }
}