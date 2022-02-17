package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.MainWindow
import dev.robocode.tankroyale.gui.ui.components.RcFrame
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import java.awt.Color
import java.awt.Font
import javax.swing.JScrollPane
import javax.swing.JTextArea


object ServerLogWindow : RcFrame("server_log_window") {

    private val textArea = JTextArea()
    private val scrollPane = JScrollPane(textArea)

    init {
        setDisposeOnEnterKeyPressed()

        setSize(700, 550)
        setLocationRelativeTo(MainWindow) // center on main window

        textArea.apply {
            isEditable = false
            foreground = Color.WHITE
            background = Color(0x28, 0x28, 0x28)

            font = Font(Font.MONOSPACED, Font.BOLD, 12)
        }
        contentPane.add(scrollPane)

        onActivated {
            // Scroll to the bottom
            val scrollBar = scrollPane.verticalScrollBar
            scrollBar.value = scrollBar.maximum
        }
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
