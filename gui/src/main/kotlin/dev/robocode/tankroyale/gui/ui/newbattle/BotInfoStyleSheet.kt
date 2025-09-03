package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.ui.extensions.ColorExt.web
import java.awt.Color
import java.awt.Font
import javax.swing.UIManager
import javax.swing.text.html.StyleSheet

class BotInfoStyleSheet : StyleSheet() {

    private val backgroundColor: Color = UIManager.getColor("Label.background") ?: Color.red
    private val foregroundColor: Color = UIManager.getColor("Label.foreground") ?: Color.red
    private val font: Font = UIManager.getFont("Label.font")

    init {
        addRule("""
            body {
                    background-color: ${backgroundColor.web};
                    color: ${foregroundColor.web};
                    font-family: sans-serif;
                    font-size: ${font.size};
                    padding: 2px;
            }
            """
        )
    }
}