package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import java.awt.Color
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea

object ServerLogWindow : JFrame(ResourceBundles.UI_TITLES.get("server_log_window")) {

    private var textArea = JTextArea()

    init {
        setSize(700, 550)
        setLocationRelativeTo(null) // center on screen

        textArea.apply {
            isEditable = false
            foreground = Color.WHITE
            background = Color(0x28, 0x28, 0x28)
        }
        val scrollPane = JScrollPane(textArea)
        contentPane.add(scrollPane)
    }

    fun clear() {
        textArea.text = null
    }

    fun append(line: String) {
        val regex = Regex("\u001B\\[[^m]+m") // TODO: Use JTextPane with colors
        val result = regex.replace(line, "")
        textArea.append(result)

        // Scroll to bottom
        textArea.caretPosition = textArea.document.length
    }
}
